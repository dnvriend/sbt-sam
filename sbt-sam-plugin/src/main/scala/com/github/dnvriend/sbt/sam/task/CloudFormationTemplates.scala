package com.github.dnvriend.sbt.sam.task

import com.github.dnvriend.sbt.aws.task.TemplateBody
import com.github.dnvriend.sbt.sam.task.Models.DynamoDb.TableWithIndex
import com.github.dnvriend.sbt.sam.task.Models.{DynamoDb, Policies}
import play.api.libs.json._

import scalaz._
import scalaz.Scalaz._

object JsMonoids {
  val jsObjectMerge: Monoid[JsValue] = Monoid.instance({
    case (l, JsNull) => l
    case (JsNull, r) => r
    case (l: JsObject, r: JsObject) => l ++ r
    case (l, _) => l
  }, JsNull)
}

object CloudFormationTemplates {
  implicit val monoid: Monoid[JsObject] = Monoid.instance(_ ++ _, Json.obj())
  val templateFormatVersion: JsObject = Json.obj("AWSTemplateFormatVersion" -> "2010-09-09")
  val samTransform: JsObject = Json.obj("Transform" -> "AWS::Serverless-2016-10-31")

  /**
    * Returns the basic cloud formation template to create the stack and deployment bucket
    */
  def deploymentBucketTemplate(config: ProjectConfiguration): TemplateBody = {
    TemplateBody.fromJson(
      templateFormatVersion ++
        resources(bucketResource("SbtSamDeploymentBucket", config.samS3BucketName.value))
    )
  }

  def updateTemplate(config: ProjectConfiguration, jarName: String, latestVersion: String): TemplateBody = {
    TemplateBody.fromJson(
      templateFormatVersion ++
        samTransform ++
        resources(
          bucketResource("SbtSamDeploymentBucket", config.samS3BucketName.value),
          parseLambdaHandlers(config.samS3BucketName, jarName, latestVersion, config.lambdas),
          parseDynamoDBResource(config.tables, config.projectName, config.samStage)
          //          ,parsePolicies(config.policies)
        ) ++
        outputs(config)
    )
  }

  /**
    * Merges a sequence of Outputs. Please note, CloudFormation templates support
    * a maximum of 60 outputs
    * see: http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/outputs-section-structure.html
    */
  private def outputs(config: ProjectConfiguration): JsObject = {
    val outputs: List[JsValue] = List(
      outputServiceEndpoint(config),
    )
    Json.obj("Outputs" -> outputs.foldMap(identity)(JsMonoids.jsObjectMerge))
  }

  /**
    * Creates the output 'ServiceEndpoint' with a value eg.
    * 'https://gm3vkzgx9b.execute-api.eu-west-1.amazonaws.com/Prod'
    */
  private def outputServiceEndpoint(config: ProjectConfiguration): JsValue = {
    if (config.lambdas.exists(_.isInstanceOf[HttpHandler])) {
      Json.obj(
        "ServiceEndpoint" -> Json.obj(
          "Description" -> "URL of the service endpoint",
          "Value" -> Json.obj(
            "Fn::Join" -> Json.arr(
              "",
              Json.arr(
                "https://",
                ServerlessParameters.ref(ServerlessParameters.ServerlessRestApi),
                ".execute-api.",
                PseudoParameters.ref(PseudoParameters.Region),
                ".",
                PseudoParameters.ref(PseudoParameters.URLSuffix),
                "/",
                ServerlessParameters.ref(ServerlessParameters.ServerlessRestApiProdStage)
              )
            )
          )
        )
      )
    } else JsNull
  }

  /**
    * Merges a sequence of Resources
    */
  private def resources(resources: JsObject*): JsObject =
    Json.obj("Resources" -> resources.reduce(_ ++ _))

  private def bucketResource(resourceName: String, bucketName: String): JsObject = {
    Json.obj(
      resourceName -> Json.obj(
        "Type" -> "AWS::S3::Bucket",
        "Properties" -> Json.obj(
          "AccessControl" -> "BucketOwnerFullControl",
          "BucketName" -> bucketName,
          "VersioningConfiguration" -> Json.obj("Status" -> "Enabled")
        )
      )
    )
  }

  private def parseLambdaHandlers(bucketName: SamS3BucketName, jarName: String, latestVersion: String, handlers: Set[LambdaHandler]): JsObject = {
    handlers.foldMap {
      case HttpHandler(lambdaConf, httpConf) ⇒
        parseLambdaHandler(
          bucketName,
          jarName,
          latestVersion,
          lambdaConf,
          apiGatewayEvent(lambdaConf.simpleClassName, httpConf)
        )
      case DynamoHandler(lambdaConf, dynamoConf) ⇒
        parseLambdaHandler(
          bucketName,
          jarName,
          latestVersion,
          lambdaConf,
          dynamoDbStreamEvent(lambdaConf.simpleClassName, dynamoConf)
        )
    }
  }

  private def parseLambdaHandler(samS3BucketName: SamS3BucketName, jarName: String, latestVersion: String, config: LambdaConfig, event: JsObject): JsObject = {
    Json.obj(
      config.simpleClassName → Json.obj(
        "Type" → "AWS::Serverless::Function",
        "Properties" → Json.obj(
          "Handler" → s"${config.fqcn}::handleRequest",
          "Runtime" → "java8",
          "CodeUri" → Json.obj(
            "Bucket" -> samS3BucketName.value,
            "Key" -> jarName,
            "Version" -> latestVersion
          ),
          "Policies" → Json.arr("AmazonDynamoDBFullAccess", "CloudWatchFullAccess", "CloudWatchLogsFullAccess"),
          "Description" → config.description,
          "MemorySize" → config.memorySize,
          "Timeout" → config.timeout,
          "Tracing" → "Active",
          "Events" → event
        )
      )
    )
  }

  private def apiGatewayEvent(eventName: String, httpConf: HttpConf): JsObject = {
    Json.obj(
      eventName -> Json.obj(
        "Type" -> "Api",
        "Properties" -> Json.obj(
          "Path" -> httpConf.path,
          "Method" -> httpConf.method
        )
      ))
  }

  private def dynamoDbStreamEvent(eventName: String, dynamoConf: DynamoConf): JsObject = {
    Json.obj(
      eventName → Json.obj(
        "Type" → "DynamoDB",
        "Properties" → Json.obj(
          "Stream" → Json.obj(
            "Fn::GetAtt" → Json.arr(dynamoConf.tableName, "StreamArn")
          ),
          "BatchSize" → dynamoConf.batchSize,
          "StartingPosition" → dynamoConf.startingPosition
        )
      ))
  }

  private def parseDynamoDBResource(tables: Set[DynamoDb.TableWithIndex], projectName: String, stage: SamStage): JsObject = {
    def streamJson(table: TableWithIndex) = table.stream match {
      case Some(s) ⇒ Json.obj(
        table.configName → Json.obj(
          "Properties" → Json.obj(
            "StreamSpecification" → Json.obj(
              "StreamViewType" → s
            )
          )
        )
      )
      case None ⇒ JsObject(Nil)
    }

    def indicesJson(table: TableWithIndex) = table.gsis match {
      case _ :: _ ⇒ Json.obj(
        table.configName → Json.obj(
          "Properties" → Json.obj(
            "GlobalSecondaryIndexes" → indexesToJson(table.gsis)
          )
        )
      )
      case Nil ⇒ JsObject(Nil)
    }

    tables.foldMap { table ⇒
      Json.obj(
        table.configName → Json.obj(
          "Type" → "AWS::DynamoDB::Table",
          "Properties" → Json.obj(
            "TableName" → s"$projectName-${stage.value}-${table.name}",
            "AttributeDefinitions" → attributeDefinitions(table),
            "KeySchema" → keySchemaToJson(table.hashKey, table.rangeKey),
            "ProvisionedThroughput" → Json.obj(
              "ReadCapacityUnits" → table.rcu,
              "WriteCapacityUnits" → table.wcu
            )
          )
        )
      ) deepMerge streamJson(table) deepMerge indicesJson(table)
    }
  }

  private def parsePolicies(policies: Set[Policies.Policy]): JsObject = {
    policies.foldMap { policy ⇒
      Json.obj(
        policy.configName → Json.obj(
          "Type" → "AWS::IAM::Policy",
          "DependsOn" → policy.dependsOn,
          "Properties" → Json.obj(
            "PolicyName" → policy.properties.name,
            "PolicyDocument" → Json.obj(
              "Version" → "2012-10-17",
              "Statement" → statementsToJson(policy.properties.statements)),
            "Roles" → rolesToJson(policy.properties.roles)
          )
        )
      )
    }
  }

  private def attributeDefinitions(table: DynamoDb.TableWithIndex): JsArray = {
    def toJson(name: String, `type`: String): JsObject = Json.obj(
      "AttributeName" → name,
      "AttributeType" → `type`
    )

    val indexKeys = table.gsis.map { index ⇒
      val indexHashKey = toJson(index.hashKey.name, index.hashKey.`type`)
      val rangeKey = index.rangeKey match {
        case Some(key) ⇒ toJson(key.name, key.`type`)
        case None ⇒ JsObject(Nil)
      }
      indexHashKey ++ rangeKey
    }

    val rangeKey = table.rangeKey match {
      case Some(key) ⇒ toJson(key.name, key.`type`)
      case None ⇒ JsObject(Nil)
    }

    val hashKey = toJson(table.hashKey.name, table.hashKey.`type`)
    val objects = (indexKeys :+ rangeKey :+ hashKey).filter(_.value != Map.empty)
    objects.foldLeft(Json.arr())((arr, a) ⇒ arr ++ Json.arr(a))
  }

  private def keySchemaToJson(hashKey: DynamoDb.HashKey, rangeKey: Option[DynamoDb.RangeKey]): JsArray = {
    val hashKeyJson = Json.obj(
      "AttributeName" → hashKey.name,
      "KeyType" → "HASH"
    )

    val rangeKeyJson = rangeKey match {
      case Some(rk) ⇒ Json.obj(
        "AttributeName" → rk.name,
        "KeyType" → "RANGE")
      case None ⇒ JsObject(Nil)
    }

    val list = List(hashKeyJson, rangeKeyJson).filter(_.value != Map.empty)
    list.foldLeft(Json.arr())((arr, a) ⇒ arr ++ Json.arr(a))
  }

  private def statementsToJson(statements: List[Policies.Statements]): JsArray = {
    statements.map { statement ⇒
      Json.obj(
        "Effect" → "Allow",
        "Action" → statement.allowedActions,
        "Resource" → statement.resource
      )
    }.foldLeft(Json.arr())((arr, a) ⇒ arr ++ Json.arr(a))
  }

  private def rolesToJson(roles: List[Policies.Role]): JsArray = {
    roles.map { role ⇒
      Json.obj("Ref" → role.ref)
    }.foldLeft(Json.arr())((arr, a) ⇒ arr ++ Json.arr(a))
  }

  private def indexesToJson(gsis: List[DynamoDb.GlobalSecondaryIndex]): JsArray = {
    val objects: List[JsObject] = gsis.map { index ⇒
      Json.obj(
        "IndexName" → index.indexName,
        "KeySchema" → keySchemaToJson(index.hashKey, index.rangeKey),
        "Projection" → Json.obj(
          "ProjectionType" → index.projectionType
        ),
        "ProvisionedThroughput" → Json.obj(
          "ReadCapacityUnits" → index.rcu,
          "WriteCapacityUnits" → index.wcu
        )
      )
    }
    objects.foldLeft(Json.arr())((arr, a) ⇒ arr ++ Json.arr(a))
  }
}

trait ServerlessParameter
object ServerlessParameters {
  def ref(param: ServerlessParameter): JsObject = {
    Json.obj("Ref" -> param.toString)
  }

  /**
    * 'AWS::ApiGateway::RestApi' eg. 'gm3vkzgx9b'
    */
  case object ServerlessRestApi extends ServerlessParameter

  /**
    * 'AWS::ApiGateway::Stage' eg. 'Prod'
    */
  case object ServerlessRestApiProdStage extends ServerlessParameter
}


trait PseudoParameter
object PseudoParameters {
  /**
    * Returns the param
    */
  def param(param: PseudoParameter): String = {
    s"AWS::$param"
  }

  def ref(p: PseudoParameter): JsObject = {
    Json.obj("Ref" -> param(p))
  }

  def output(name: String, p: PseudoParameter): JsObject = {
    Json.obj(name -> ref(p))
  }

  /**
    * Returns a string representing the AWS Region in which the encompassing resource is being created, such as us-west-2.
    */
  case object Region extends PseudoParameter

  /**
    * Returns the AWS account ID of the account in which the stack is being created
    */
  case object AccountId extends PseudoParameter

  /**
    * Returns the list of notification Amazon Resource Names (ARNs) for the current stack.
    */
  case object NotificationArn extends PseudoParameter

  /**
    * Returns the partition that the resource is in. For standard AWS regions, the partition is aws.
    * For resources in other partitions, the partition is aws-partitionname. For example, the partition
    * for resources in the China (Beijing) region is aws-cn.
    */
  case object Partition extends PseudoParameter

  /**
    * Returns the name of the stack as specified with the aws cloudformation create-stack command, such as teststack.
    */
  case object StackName extends PseudoParameter

  /**
    * Returns the suffix for a domain. The suffix is typically amazonaws.com, but might differ by region.
    * For example, the suffix for the China (Beijing) region is amazonaws.com.cn.
    */
  case object URLSuffix extends PseudoParameter
}

object CloudFormation {
  def properties(props: JsValue*): JsObject = {
    Json.obj("Properties" -> props.toList.foldMap(identity)(JsMonoids.jsObjectMerge))
  }

  /**
    * The intrinsic function Ref returns the value of the specified parameter or resource.
    *
    * - When you specify a parameter's logical name, it returns the value of the parameter.
    * - When you specify a resource's logical name, it returns a value that you can typically
    *   use to refer to that resource, such as a physical ID.
    *
    * see: http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-ref.html
    */
  def ref(logicalName: String): JsObject = {
    Json.obj("Ref" -> logicalName)
  }

  /**
    * The 'Fn::GetAtt' intrinsic function returns the value of an attribute from a resource in the template.
    *
    * see: http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html
    */
  def getAtt(logicalName: String, attributeName: String): JsObject = {
    Json.obj("Fn::GetAtt" -> Json.arr(logicalName, attributeName))
  }
}

object Cognito {
  object UserPool {
    /**
      * Returns a CloudFormation configuration based on the ProjectConfiguration
      */
    def resource(config: ProjectConfiguration): JsObject = {
      val logicalResourceId = "" //todo: set resource id name
      Json.obj(
        logicalResourceId -> (Json.obj(
          "Type" -> "AWS::Cognito::UserPool"
        ) ++ CloudFormation.properties(
          propUserPoolName(config),
          propAdminCreateUserConfig(config),
          propPolicies(config),
        ))
      )
    }

    /**
      * A string used to name the user pool.
      */
    def propUserPoolName(config: ProjectConfiguration): JsValue = {
      Json.obj("UserPoolName" -> "auth_pool") //todo: set userpool name
    }

    /**
      * The type of configuration for creating a new user profile.
      */
    def propAdminCreateUserConfig(config: ProjectConfiguration): JsValue = {
      Json.obj("AdminCreateUserConfig" -> Json.obj(
        "AllowAdminCreateUserOnly" -> true,
        "UnusedAccountValidityDays" -> 30
      ))
    }

    /**
      * The policies associated with the Amazon Cognito user pool.
      */
    def propPolicies(config: ProjectConfiguration): JsValue = {
      Json.obj("Policies" ->
        Json.obj(
          "MinimumLength" -> 6,
          "RequireLowercase" -> true,
          "RequireNumbers" -> false,
          "RequireSymbols" -> false,
          "RequireUppercase" -> false
        )
      )
    }

    /**
      * When the logical ID of this resource is provided to the Ref intrinsic function, Ref returns a generated ID,
      * such as 'us-east-2_zgaEXAMPLE'
      */
    def logicalId(config: ProjectConfiguration): JsValue = {
      val logicalResourceId = "" //todo: set the resource id name
      CloudFormation.ref(logicalResourceId)
    }

    /**
      * The provider name of the Amazon Cognito user pool, specified as a String.
      */
    def providerName(config: ProjectConfiguration): JsValue = {
      val logicalResourceId = "" //todo: set the resource id name
      CloudFormation.getAtt(logicalResourceId, "ProviderName")
    }

    /**
      * The URL of the provider of the Amazon Cognito user pool, specified as a String.
      */
    def providerUrl(config: ProjectConfiguration): JsValue = {
      val logicalResourceId = "" //todo: set the resource id name
      CloudFormation.getAtt(logicalResourceId, "ProviderURL")
    }

    /**
      * The Amazon Resource Name (ARN) of the user pool, such as
      * 'arn:aws:cognito-idp:us-east-2:123412341234:userpool/us-east-1 _123412341'
      */
    def arn(config: ProjectConfiguration): JsValue = {
      val logicalResourceId = "" //todo: set the resource id name
      CloudFormation.getAtt(logicalResourceId, "Arn")
    }
  }

  /**
    * creates an Amazon Cognito user pool client.
    */
  object UserPoolClient {
    def resource(config: ProjectConfiguration): JsObject = {
      val logicalResourceId = "" //todo: set resource id name
      Json.obj(
        logicalResourceId -> (Json.obj(
          "Type" -> "AWS::Cognito::UserPoolClient",
        ) ++ CloudFormation.properties(
          propClientName(config),
          propExplicitAuthFlows(config),
          propUserPoolId(config),
        ))
      )
    }

    /**
      * The client name for the user pool client that you want to create.
      */
    def propClientName(config: ProjectConfiguration): JsObject = {
      val clientName = "" //todo: set the client name
      Json.obj("ClientName" -> clientName)
    }

    /**
      * The explicit authentication flows, which can be one of the following:
      * - ADMIN_NO_SRP_AUTH
      * - CUSTOM_AUTH_FLOW_ONLY.
      */
    def propExplicitAuthFlows(config: ProjectConfiguration): JsObject = {
      Json.obj("ExplicitAuthFlows" -> Json.arr("ADMIN_NO_SRP_AUTH"))
    }

    /**
      * The user pool ID for the user pool where you want to create a client.
      */
    def propUserPoolId(config: ProjectConfiguration): JsObject = {
      Json.obj("UserPoolId" -> Cognito.UserPool.logicalId(config))
    }
  }
}
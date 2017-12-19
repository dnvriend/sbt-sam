package com.github.dnvriend.sbt.sam.task

import com.github.dnvriend.sbt.aws.task.TemplateBody
import com.github.dnvriend.sbt.sam.task.Models.DynamoDb.TableWithIndex
import com.github.dnvriend.sbt.sam.task.Models.{DynamoDb, Policies}
import play.api.libs.json._

import scalaz._
import scalaz.Scalaz._

object CloudFormationTemplates {
  implicit val monoid: Monoid[JsObject] = Monoid.instance(_ ++ _, Json.obj())
  val jsObjectMerge: Monoid[JsValue] = Monoid.instance({
    case (l, JsNull) => l
    case (JsNull, r) => r
    case (l: JsObject, r: JsObject) => l ++ r
    case (l, _) => l
  }, JsNull)
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
      outputApiGateway(config),
    )
    Json.obj("Outputs" -> outputs.foldMap(identity)(jsObjectMerge))
  }

  private def outputApiGateway(config: ProjectConfiguration): JsValue = {
    val regionName: String = config.credentialsRegionAndUser.credentialsAndRegion.region.getName
    val stage: String = "Prod"
    if(config.lambdas.exists(_.isInstanceOf[HttpHandler])) {
      Json.obj(
        "ServiceEndpoint" -> Json.obj(
          "Description" -> "URL of the service endpoint",
          "Value" -> Json.obj(
            "Fn::Join" -> Json.arr(
              "",
              Json.arr(
                "https://",
                Json.obj("Ref" -> "ServerlessRestApi"),
                s".execute-api.$regionName.amazonaws.com/$stage"
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
        case None      ⇒ JsObject(Nil)
      }
      indexHashKey ++ rangeKey
    }

    val rangeKey = table.rangeKey match {
      case Some(key) ⇒ toJson(key.name, key.`type`)
      case None      ⇒ JsObject(Nil)
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

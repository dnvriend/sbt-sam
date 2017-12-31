package com.github.dnvriend.sbt.sam.task

import com.amazonaws.regions.Regions
import com.github.dnvriend.sbt.aws.task.{AccountId, TemplateBody}
import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.generic.tag.ResourceTag
import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.sam.cf.resource.apigw.{ServerlessApi, ServerlessApiProperties, ServerlessApiStageName, ServerlessApiSwaggerDefinitionBody}
import com.github.dnvriend.sbt.sam.cf.resource.dynamodb._
import com.github.dnvriend.sbt.sam.cf.resource.firehose.s3._
import com.github.dnvriend.sbt.sam.cf.resource.iam.policy._
import com.github.dnvriend.sbt.sam.cf.resource.kinesis.CFKinesisStream
import com.github.dnvriend.sbt.sam.cf.resource.lambda.ServerlessFunction
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.apigw.ApiGatewayEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.dynamodb.DynamoDBEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.kinesis.KinesisEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.schedule.ScheduledEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.sns.SnsEventSource
import com.github.dnvriend.sbt.sam.cf.resource.s3._
import com.github.dnvriend.sbt.sam.cf.resource.sns.CFTopic
import com.github.dnvriend.sbt.sam.cf.template._
import com.github.dnvriend.sbt.sam.cf.template.output.{GenericOutput, ServerlessApiOutput}
import com.github.dnvriend.sbt.sam.resource.bucket.model.S3Bucket
import com.github.dnvriend.sbt.sam.resource.dynamodb.model.{HashKey, RangeKey, TableWithIndex}
import com.github.dnvriend.sbt.sam.resource.firehose.s3.model.S3Firehose
import com.github.dnvriend.sbt.sam.resource.kinesis.model.KinesisStream
import com.github.dnvriend.sbt.sam.resource.role.model.IamRole
import com.github.dnvriend.sbt.sam.resource.sns.model.Topic
import play.api.libs.json._

import scalaz._
import scalaz.Scalaz._

object CloudFormationTemplates {
  /**
   * Returns the basic cloud formation template to create the stack and deployment bucket
   */
  def deploymentBucketTemplate(config: ProjectConfiguration): TemplateBody = {
    val template: CloudFormationTemplate = CloudFormationTemplate(
      Description.description(config.projectDescription),
      Resources.fromResources(
        CFS3Bucket.deploymentBucket(
          config.samS3BucketName.value,
          ResourceTag.projectTags(
            config.projectName,
            config.projectVersion,
            config.samStage.value
          )
        )
      )
    )
    TemplateBody.fromJson(Json.toJson(template))
  }

  /**
   * A resource, like a Kinesis stream, SNS topic or DynamoDB table, has a name, eg. a
   * stream has a stream-name, a topic has a topic-name and a table has a table-name, and
   * these names are logical defined in an SBT-SAM project like 'person', but in an AWS account,
   * these names must be unique because per resource type there is a flat namespace. By scoping a
   * resource name like table-name, with [projectname]-[stage]-[name], a name becomes unique in the
   * resource namespace.
   */
  def createResourceName(projectName: String, stage: String, resourceName: String): String = {
    s"$projectName-$stage-$resourceName".toLowerCase.trim
  }

  /**
   * Sbt SAM uses the SAM-Model, that needs a transform in order to use the SAM-DSL in CloudFormation templates.
   */
  def updateTemplate(config: ProjectConfiguration, jarName: String, latestVersion: String): TemplateBody = {
    val projectName = config.projectName
    val projectVersion = config.projectVersion
    val stage = config.samStage.value
    val deploymentBucketName = config.samS3BucketName.value

    val resources: List[Resource] = {
      kinesisResources(projectName, projectVersion, stage, config.streams) ++
        snsResources(projectName, stage, config.topics) ++
        dynamoDBResources(projectName, projectVersion, stage, config.tables) ++
        determineEventHandlerResources(projectName, projectVersion, stage, deploymentBucketName, jarName, latestVersion, config.lambdas) ++
        bucketResources(projectName, projectVersion, stage, config.buckets) ++
        s3FirehoseResources(projectName, projectVersion, stage, config.userArn.accountId, config.getRegion, config.s3Firehoses) ++
        iamRolesResources(projectName, projectVersion, stage, config.iamRoles) ++
        apiGatewayResource(projectName, stage, config.httpHandlers)
    }

    val template: CloudFormationTemplate = CloudFormationTemplate(
      Description(config.samCFTemplateName.value),
      Resources.fromResources(
        samDeploymentBucket(projectName, projectVersion, stage, deploymentBucketName),
        resources
      ),
      Option(Transform.samTransform),
      determineOutputs(
        projectName,
        stage,
        config.topics,
        config.buckets,
        config.streams,
        config.s3Firehoses,
        config.existHttpHandlers,
      )
    )
    TemplateBody.fromJson(Json.toJson(template))
  }

  /**
   * Determine SNS CloudFormation resources
   */
  def snsResources(projectName: String, stage: String, topics: List[Topic]): List[Resource] = {
    topics.map(t => CFTopic.fromConfig(
      t.configName,
      t.displayName,
      createResourceName(projectName, stage, t.name))
    )
  }

  /**
   * Determine Kinesis CloudFormation resources
   */
  def kinesisResources(projectName: String, projectVersion: String, stage: String, streams: List[KinesisStream]): List[Resource] = {
    streams.map(str => CFKinesisStream.fromConfig(
      str.configName,
      createResourceName(projectName, stage, str.name),
      str.retensionPeriodHours,
      str.shardCount,
      projectName,
      projectVersion,
      stage)
    )
  }

  /**
   * Determine the SAM S3 deployment bucket CloudFormation resource
   */
  def samDeploymentBucket(projectName: String, projectVersion: String, stage: String, samS3BucketName: String): CFS3Bucket = {
    CFS3Bucket.deploymentBucket(
      samS3BucketName,
      ResourceTag.projectTags(projectName, projectVersion, stage)
    )
  }

  /**
   * Determine S3 bucket resource
   */
  def bucketResource(projectName: String, projectVersion: String, stage: String, bucket: S3Bucket): Resource = {
    CFS3Bucket(
      bucket.configName,
      S3AccessControl.fromName(bucket.accessControl),
      createResourceName(projectName, stage, bucket.name),
      VersioningConfigurationOption.fromBoolean(bucket.versioningEnabled),
      ResourceTag.projectTags(projectName, projectVersion, stage),
      bucket.website.map(website => CFS3WebsiteConfiguration(website.indexDocument, website.errorDocument)),
      if(bucket.corsEnabled) Option(CorsRules(CorsRule.AllowAllForWebsiteBucketCorsRules)) else None
    )
  }

  /**
   * Determine S3 bucket resources
   */
  def bucketResources(projectName: String, projectVersion: String, stage: String, buckets: List[S3Bucket]): List[Resource] = {
    buckets.map(bucket => bucketResource(projectName, projectVersion, stage, bucket))
  }

  /**
    * Determine Iam Role resource
    */
  def iamRoleResource(projectName: String, projectVersion: String, stage: String, role: IamRole): Resource = {
    CFIamRole(
      role.configName,
      createResourceName(projectName, stage, role.name),
      CFIamPolicyDocument(List(
        CFIamStatement.allowAssumeRole(CFPrincipal(role.principalServiceName))
      )),
      role.managedPolicyArns
    )
  }

  /**
    * determine Iam Roles resources
    */
  def iamRolesResources(projectName: String, projectVersion: String, stage: String, roles: List[IamRole]): List[Resource] = {
    roles.map(role => iamRoleResource(projectName, projectVersion, stage, role))
  }

  /**
    * Determine S3 Kinesis Data Firehose Resource
    */
  def s3FirehoseResource(projectName: String,
                         projectVersion: String,
                         stage: String,
                         accountId: AccountId,
                         regions: Regions,
                         firehose: S3Firehose): Resource = {
    CFS3Firehose(
      firehose.configName,
      createResourceName(projectName, stage, firehose.name),
      CFS3FirehoseBucketArn.fromConfig(createResourceName(projectName, stage, firehose.bucketName)),
      CFS3FirehoseKinesisStreamSourceConfiguration.fromConfig(accountId.value, regions.getName, createResourceName(projectName, stage, firehose.kinesisStreamSource), firehose.roleArn),
      CFS3FirehoseKinesisStreamBufferingHints(firehose.bufferingIntervalInSeconds, firehose.bufferingSize),
      None,
      CFS3FirehoseCompressionFormat(firehose.compression),
      None
    )
  }

  /**
    * Determine S3 Kinesis Data Firehose resources
    */
  def s3FirehoseResources(projectName: String,
                          projectVersion: String,
                          stage: String,
                          accountId: AccountId,
                          regions: Regions,
                          s3Firehoses: List[S3Firehose]): List[Resource] = {
    s3Firehoses.map(s3Firehose => s3FirehoseResource(projectName, projectVersion, stage, accountId, regions, s3Firehose))
  }

  /**
   * Determine the DynamoDB CloudFormation resource
   */
  def dynamoDBResource(projectName: String, projectVersion: String, stage: String, table: TableWithIndex): Resource = {
    val tableName: CFTDynamoDBTableName = CFTDynamoDBTableName(createResourceName(projectName, stage, table.name))
    def keySchema(hashKey: HashKey, rangeKey: Option[RangeKey]): CFDynamoDBTableKeySchema = {
      CFDynamoDBTableKeySchema(
        CFDynamoDbTableHashKey(hashKey.name),
        rangeKey.map(key => CFDynamoDbTableRangeKey(key.name))
      )
    }
    def provisionedThroughput(rcu: Int, wcu: Int): CFDynamoDBTableProvisionedThroughput = {
      CFDynamoDBTableProvisionedThroughput(rcu, wcu)
    }
    val attributeDefinition: CFDynamoDBTableAttributeDefinitions = {
      val attributes: List[CFDynamoDBTableAttributeDefinition] = {
        Set(CFDynamoDBTableAttributeDefinition(table.hashKey.name, table.hashKey.keyType)) ++
          table.rangeKey.map(key => CFDynamoDBTableAttributeDefinition(key.name, key.keyType)) ++
          table.gsis.map(_.hashKey).map(key => CFDynamoDBTableAttributeDefinition(key.name, key.keyType)) ++
          table.gsis.flatMap(_.rangeKey.map(key => CFDynamoDBTableAttributeDefinition(key.name, key.keyType)))
      }.toList
      CFDynamoDBTableAttributeDefinitions(attributes)
    }
    val streamSpec: Option[CFDynamoDBTableStreamSpecification] = table.stream.map(spec => CFDynamoDBTableStreamSpecification(spec))
    val gsis: Option[CFDynamoDBTableGlobalSecondaryIndexes] = {
      val indexes: List[CFDynamoDBTableGlobalSecondaryIndex] = table.gsis.map { index =>
        CFDynamoDBTableGlobalSecondaryIndex(
          index.indexName,
          keySchema(index.hashKey, index.rangeKey),
          index.projectionType,
          provisionedThroughput(index.rcu, index.wcu)
        )
      }
      indexes.toNel.map(xs => CFDynamoDBTableGlobalSecondaryIndexes(xs.toList))
    }
    val tableProperties: CFDynamoDBTableProperties = {
      CFDynamoDBTableProperties(
        tableName,
        keySchema(table.hashKey, table.rangeKey),
        attributeDefinition,
        provisionedThroughput(table.rcu, table.wcu),
        streamSpec,
        gsis,
        CFDynamoDBTableTags(ResourceTag.projectTags(
          projectName,
          projectVersion,
          stage
        )
        ))
    }
    CFDynamoDBTable(
      table.configName,
      tableProperties
    )
  }

  /**
   * Determine all DynamoDB CloudFormation resources
   */
  def dynamoDBResources(projectName: String, projectVersion: String, stage: String, tables: List[TableWithIndex]): List[Resource] = {
    tables.map(table => dynamoDBResource(projectName, projectVersion, stage, table))
  }

  /**
   * Determine - the single - ServerlessAPI CloudFormation resource
   */
  def apiGatewayResource(projectName: String, stage: String, httpHandlers: List[HttpHandler]): Option[ServerlessApi] = {
    httpHandlers.toNel.map { handlers =>
      ServerlessApi(
        ServerlessApiProperties(
          ServerlessApiStageName(stage),
          ServerlessApiSwaggerDefinitionBody(
            projectName,
            stage,
            handlers.toList
          )
        )
      )
    }
  }

  /**
   * Determine the event sources that will be attached to a CloudFormation Serverless::Function
   */
  def determineEventSourceForLambdaHandler(projectName: String, stage: String, handler: LambdaHandler): EventSource = handler match {
    case HttpHandler(lambdaConf, conf) =>
      ApiGatewayEventSource("ApiGatewayEventSource", conf.path, conf.method)
    case DynamoHandler(lambdaConf, conf) =>
      DynamoDBEventSource("DynamoDBEventSource", conf.tableName, conf.batchSize, conf.startingPosition)
    case ScheduledEventHandler(lambdaConf, conf) =>
      ScheduledEventSource("ScheduledEventSource", conf.schedule)
    case SNSEventHandler(lambdaConf, conf) =>
      SnsEventSource("SNSEventSource", createResourceName(projectName, stage, conf.topic))
    case KinesisEventHandler(lambdaConf, conf) =>
      KinesisEventSource("KinesisEventSource", createResourceName(projectName, stage, conf.stream), conf.batchSize, conf.startingPosition)
  }

  /**
   * Determine the CloudFormation Serverless::Function resource
   */
  def lambdaResource(
    projectName: String,
    projectVersion: String,
    stage: String,
    deploymentBucketName: String,
    jarName: String,
    latestVersion: String,
    handler: LambdaHandler): Resource = {
    val conf: LambdaConfig = handler.lambdaConfig
    ServerlessFunction(
      conf.simpleClassName,
      conf.fqcn,
      projectName,
      projectVersion,
      stage,
      deploymentBucketName,
      jarName,
      latestVersion,
      conf.description,
      conf.memorySize,
      conf.timeout,
      determineEventSourceForLambdaHandler(projectName, stage, handler)
    )
  }

  /**
   * Determine all Serverless::Function CloudFormation resources
   */
  def determineEventHandlerResources(
    projectName: String,
    projectVersion: String,
    stage: String,
    deploymentBucketName: String,
    jarName: String,
    latestVersion: String,
    handlers: List[LambdaHandler]): List[Resource] = {
    handlers.map(handler => lambdaResource(projectName, projectVersion, stage, deploymentBucketName, jarName, latestVersion, handler))
  }

  /**
   * Determine CloudFormation outputs
   */
  def determineOutputs(
                      projectName: String,
                      stage: String,
                      topics: List[Topic],
                      buckets: List[S3Bucket],
                      streams: List[KinesisStream],
                      s3Firehoses: List[S3Firehose],
                      exposeApiEndpoint: Boolean,
                       ): Option[Outputs] = {
    val endpointOutput = determineApiEndpointOutput(stage, exposeApiEndpoint)
    val topicOutputs = topics.map(topic => determineTopicOuput(projectName, stage, topic))
    val bucketsOutput = buckets.map(bucket => determineBucketOutput(projectName, stage, bucket))
    val streamsOutput = streams.map(stream => determineStreamOutput(projectName, stage, stream))
    val s3FirehosesOutput = s3Firehoses.map(s3Firehose => determineS3FirehoseOutput(projectName, stage, s3Firehose))

    val listOfValidatedOutputs = (endpointOutput +: topicOutputs) ++ bucketsOutput ++ streamsOutput ++ s3FirehosesOutput
    val validated = listOfValidatedOutputs.sequenceU
    if(validated.isFailure) {
      val message: String = validated.swap.foldMap(_.intercalate1(","))
      println(message)
    }
    listOfValidatedOutputs.flatMap(_.toOption).toNel.map(nel => Outputs(nel.toList))
  }

  def determineApiEndpointOutput(stage: String, exposeApiEndpoint: Boolean): ValidationNel[String, Output] = {
    Validation.lift(!exposeApiEndpoint)(identity, "No Api endpoint to expose").map { _ =>
      ServerlessApiOutput(stage)
    }.toValidationNel
  }

  def determineTopicOuput(projectName: String, stage: String, topic: Topic): ValidationNel[String, Output] = {
    val topicName: String = createResourceName(projectName, stage, topic.name)
    Validation.lift(!topic.export)(identity, s"SNS Topic: '$topicName, is not exported").map { _ =>
      val description: String = s"SNS Topic export for project: '$projectName', for stage: '$stage'"
      GenericOutput(description, topicName, CloudFormation.snsArn(topicName))
    }.toValidationNel
  }

  def determineBucketOutput(projectName: String, stage: String, bucket: S3Bucket): ValidationNel[String, Output] = {
    val bucketName: String = createResourceName(projectName, stage, bucket.name)
    Validation.lift(!bucket.export)(identity, s"S3 Bucket: '$bucketName, is not exported").map { _ =>
      val description: String = s"S3 Bucket export for project: '$projectName', for stage: '$stage'"
      GenericOutput(description, bucketName, JsString(S3Bucket.arn(bucketName)))
    }.toValidationNel
  }

  def determineStreamOutput(projectName: String, stage: String, stream: KinesisStream): ValidationNel[String, Output] = {
    val streamName: String = createResourceName(projectName, stage, stream.name)
    Validation.lift(!stream.export)(identity, s"Kinesis Stream: '$streamName, is not exported").map { _ =>
      val description: String = s"Kinesis Stream export for project: '$projectName', for stage: '$stage'"
      GenericOutput(description, streamName, CloudFormation.kinesisArn(streamName))
    }.toValidationNel
  }

  def determineS3FirehoseOutput(projectName: String, stage: String, s3Firehose: S3Firehose): ValidationNel[String, Output] = {
    val s3FirehoseName: String = createResourceName(projectName, stage, s3Firehose.name)
    Validation.lift(!s3Firehose.export)(identity, s"S3 Firehose: '$s3FirehoseName, is not exported").map { _ =>
      val description: String = s"Kinesis Stream export for project: '$projectName', for stage: '$stage'"
      GenericOutput(description, s3FirehoseName, CloudFormation.firehoseDeliveryStreamArn(s3FirehoseName))
    }.toValidationNel
  }
}
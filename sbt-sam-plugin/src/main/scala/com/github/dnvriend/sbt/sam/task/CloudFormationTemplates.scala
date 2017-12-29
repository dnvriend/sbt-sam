package com.github.dnvriend.sbt.sam.task

import com.github.dnvriend.sbt.aws.task.TemplateBody
import com.github.dnvriend.sbt.sam.cf.generic.tag.ResourceTag
import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.sam.cf.resource.apigw.{ ServerlessApi, ServerlessApiProperties, ServerlessApiStageName, ServerlessApiSwaggerDefinitionBody }
import com.github.dnvriend.sbt.sam.cf.resource.dynamodb._
import com.github.dnvriend.sbt.sam.cf.resource.kinesis.CFKinesisStream
import com.github.dnvriend.sbt.sam.cf.resource.lambda.ServerlessFunction
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.EventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.apigw.ApiGatewayEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.dynamodb.DynamoDBEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.kinesis.KinesisEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.schedule.ScheduledEventSource
import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.sns.SnsEventSource
import com.github.dnvriend.sbt.sam.cf.resource.s3.S3Bucket
import com.github.dnvriend.sbt.sam.cf.resource.sns.CFTopic
import com.github.dnvriend.sbt.sam.cf.template._
import com.github.dnvriend.sbt.sam.cf.template.output.ServerlessApiOutput
import com.github.dnvriend.sbt.sam.resource.dynamodb.model.{ HashKey, RangeKey, TableWithIndex }
import com.github.dnvriend.sbt.sam.resource.kinesis.model.KinesisStream
import com.github.dnvriend.sbt.sam.resource.sns.model.Topic
import play.api.libs.json._

import scalaz.Scalaz._

object CloudFormationTemplates {
  /**
   * Returns the basic cloud formation template to create the stack and deployment bucket
   */
  def deploymentBucketTemplate(config: ProjectConfiguration): TemplateBody = {
    val template: CloudFormationTemplate = CloudFormationTemplate(
      Description(config.samCFTemplateName.value),
      Resources.fromResources(
        S3Bucket.deploymentBucket(
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
    s"$projectName-$stage-$resourceName".toLowerCase
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
        apiGatewayResource(projectName, stage, config.httpHandlers)
    }

    val template: CloudFormationTemplate = CloudFormationTemplate(
      Description(config.samCFTemplateName.value),
      Resources.fromResources(
        samDeploymentBucket(projectName, projectVersion, stage, deploymentBucketName),
        resources
      ),
      Option(Transform.samTransform),
      determineOutputs(config.existHttpHandlers, stage)
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
  def samDeploymentBucket(projectName: String, projectVersion: String, stage: String, samS3BucketName: String): S3Bucket = {
    S3Bucket.deploymentBucket(
      samS3BucketName,
      ResourceTag.projectTags(projectName, projectVersion, stage)
    )
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
  def determineOutputs(existsLambdaHandlers: Boolean, stage: String): Option[Outputs] = {
    if (existsLambdaHandlers) Option(Outputs(List(ServerlessApiOutput(stage)))) else None
  }
}
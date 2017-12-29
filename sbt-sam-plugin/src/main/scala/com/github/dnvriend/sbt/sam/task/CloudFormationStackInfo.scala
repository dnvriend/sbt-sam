package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.{ Stack, StackResource }
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.TableDescription
import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.FunctionConfiguration
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{ AccessControlList, Bucket }
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.Topic
import com.github.dnvriend.sbt.aws.task._
import sbt.util.Logger

import scala.collection.JavaConverters._
import scalaz._
import scalaz.Scalaz._

object CloudFormationStackResource {
  implicit val show: Show[CloudFormationStackResource] = Show.shows(m => {
    s"${m.logicalResourceId}- ${m.resourceType}- ${m.resourceStatus}- ${m.timestamp}"
  })
  def fromStackResource(resource: StackResource): CloudFormationStackResource = {
    CloudFormationStackResource(
      resource.getLogicalResourceId,
      resource.getResourceType,
      resource.getResourceStatus,
      resource.getTimestamp.toString
    )
  }
}
case class CloudFormationStackResource(
    logicalResourceId: String,
    resourceType: String,
    resourceStatus: String,
    timestamp: String)

object CloudFormationStackInfo {
  def run(
    config: ProjectConfiguration,
    stack: Option[Stack],
    client: AmazonCloudFormation,
    dynamoClient: AmazonDynamoDB,
    snsClient: AmazonSNS,
    kinesisClient: AmazonKinesis,
    lambdaClient: AWSLambda,
    s3Client: AmazonS3,
    log: Logger
  ): Unit = {

    val projectName: String = config.projectName
    val stackName: String = config.samCFTemplateName.value
    val stage: String = config.samStage.value
    val samStack: Option[SamStack] = stack.map(SamStack.fromStack)

    val stackSummary: String = {
      samStack.fold(Console.YELLOW + s"Stack '$stackName' is not yet deployed") { info =>
        val stack: Stack = info.stack
        val stackName: String = stack.getStackName
        val serviceEndpoint: String = {
          info.serviceEndpoint.fold(Console.YELLOW + "No endpoint")(endpoint => Console.GREEN + endpoint.value)
        }
        val statusReason: String = Option(stack.getStackStatusReason).filter(_ != "null").getOrElse("No status reason")
        val description: String = Option(stack.getDescription).filter(_ != "null").getOrElse("No description")
        val stackStatus = if (stack.getStackStatus.contains("COMPLETE")) Console.GREEN + stack.getStackStatus else stack.getStackStatus
        val lastUpdated: String = stack.getLastUpdatedTime.toString
        s"""Name: $stackName
           |Description: $description
           |Status: $stackStatus
           |Status reason: $statusReason
           |Last updated: $lastUpdated
           |ServiceEndpoint: $serviceEndpoint""".stripMargin
      }
    }

    val kinesisStreamsSummary: String = {
      def reportShard(conf: com.amazonaws.services.kinesis.model.Shard): String = {
        import conf._
        s"""  - Shard: $getShardId
           |    - HashKeyRange: $getHashKeyRange
           |    - SequenceNumberRange: $getSequenceNumberRange""".stripMargin
      }
      def report(conf: com.amazonaws.services.kinesis.model.StreamDescription): String = {
        import conf._
        val shardSummary: String = getShards.asScala.map(reportShard).toList.intercalate("\n")
        s"""
           |  - StreamName: $getStreamName
           |  - StreamArn: $getStreamARN
           |  - StreamStatus: $getStreamStatus
           |  - RetentionPeriodHours: $getRetentionPeriodHours
           |  - KeyId: $getKeyId
           |  - StreamCreation: $getStreamCreationTimestamp
           |  - Number of shards: ${getShards.size}
           |$shardSummary""".stripMargin
      }
      config.streams.map { stream =>
        val streamName = s"$projectName-$stage-${stream.name}"
        (stream, KinesisOperations.describeStream(streamName, kinesisClient))
      }.map {
        case (stream, optionalInfo) =>
          val info = optionalInfo.fold(Console.YELLOW + "not yet deployed")(report)
          s"* ${Console.GREEN}${stream.name}: ${Console.RESET}$info"
      }.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No streams configured")
    }

    val snsTopicsSummary: String = {
      def report(topic: Topic): String = {
        import topic._
        s"""
           |  - TopicArn: $getTopicArn""".stripMargin
      }
      config.topics.map { topic =>
        val topicName = s"$projectName-$stage-${topic.name}"
        (topic, SNSOperations.describeTopic(topicName, snsClient))
      }.map {
        case (topic, optionalInfo) =>
          val info = optionalInfo.fold(Console.YELLOW + "not yet deployed")(report)
          s"* ${Console.GREEN}${topic.name}: ${Console.RESET}$info"
      }.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No topics configured")
    }

    val bucketsSummary: String = {
      def report(bucket: Bucket, location: String, acl: AccessControlList) = {
        import bucket._
        val arn: String = s"arn:aws:s3:::$getName"
        val grants = acl.getGrantsAsList.asScala.toList.foldMap(_.getPermission.toString)
        s"""
           |  - BucketArn: $arn
           |  - BucketName: $getName
           |  - grants: $grants
           |  - Location: $location
           |  - CreationDate: $getCreationDate""".stripMargin
      }
      config.buckets.map { bucket =>
        val bucketName = BucketName(s"$projectName-$stage-${bucket.name}")
        val s3bucket: ValidationNel[String, Bucket] = S3Operations.getBucket(bucketName, s3Client).toSuccessNel(s"S3 bucket not found for name '${bucketName.value}'")
        val location: ValidationNel[String, String] = S3Operations.getBucketLocation(bucketName, s3Client).toSuccessNel(s"S3 location not found for name '${bucketName.value}'")
        val acl: ValidationNel[String, AccessControlList] = S3Operations.getBucketACL(bucketName, s3Client).toSuccessNel(s"S3 bucket acl not found for name '${bucketName.value}'")
        val bucketInfo = (s3bucket |@| location |@| acl)((_, _, _)).toOption
        (bucket, bucketInfo)
      }.map {
        case (bucket, optionalInfo) =>
          val info = optionalInfo.fold(Console.YELLOW + "not yet deployed")(report _ tupled)
          s"* ${Console.GREEN}${bucket.name}: ${Console.RESET}$info"
      }.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No buckets configured")
    }

    val tablesSummary: String = {
      def report(conf: TableDescription): String = {
        import conf._
        s"""
           |  - TableName: $getTableName
           |  - TableId: $getTableId
           |  - TableArn: $getTableArn
           |  - Table status: $getTableStatus
           |  - Number of items: $getItemCount
           |  - TableSizeBytes: $getTableSizeBytes
           |  - TableCreation: $getCreationDateTime
           |  - ProvisionedThroughput: $getProvisionedThroughput
           |  - KeySchema: $getKeySchema
           |  - Attribute Definitions: $getAttributeDefinitions
           |  - Local Secondary Indexes: $getLocalSecondaryIndexes
           |  - Global Secondary Indexes: $getGlobalSecondaryIndexes
           |  - StreamArn: $getLatestStreamArn
           |  - StreamLabel: $getLatestStreamLabel
           |  - Restore Summary: $getRestoreSummary""".stripMargin
      }
      config.tables.map(table => {
        val tableName = s"$projectName-$stage-${table.name}"
        (table, DynamoDbOperations.describeTable(tableName, dynamoClient))
      }).map {
        case (table, optionalInfo) =>
          val info = optionalInfo.fold(Console.YELLOW + "not yet deployed")(report)
          s"* ${Console.GREEN}${table.name}: ${Console.RESET}$info"
      }.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No tables configured")
    }

    val lambdaSummary: String = {
      def report(conf: FunctionConfiguration): String = {
        import conf._
        s"""
           |  - Arn: $getFunctionArn
           |  - Role: $getRole
           |  - Handler: $getHandler
           |  - size: $getCodeSize
           |  - timeout: $getTimeout
           |  - memory: $getMemorySize
           |  - tracing: ${getTracingConfig.getMode}
           |  - lastmodified: $getLastModified""".stripMargin
      }
      val httpHandlers: String = {
        config.lambdas.collect({ case h: HttpHandler => h }).map { handler =>
          val fqcn: String = handler.lambdaConfig.fqcn
          (handler, AwsLambdaOperations.findFunction(fqcn, projectName, stage, lambdaClient))
        }.map {
          case (handler, optionalInfo) =>
            val projectionStatus = optionalInfo.fold(Console.YELLOW + "not yet deployed")(report)
            val handlerName: String = handler.lambdaConfig.simpleClassName
            val handlerPath: String = handler.httpConf.path
            val handlerMethod: String = handler.httpConf.method.toUpperCase
            val handlerInfo: String = s"$handlerName: ($handlerMethod -> '$handlerPath')"
            s"* ${Console.GREEN}$handlerInfo: ${Console.RESET}$projectionStatus"
        }.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No Api Http Event handlers configured")
      }
      val dynamoHandlers: String = {
        config.lambdas.collect({ case h: DynamoHandler => h }).map { handler =>
          val fqcn: String = handler.lambdaConfig.fqcn
          (handler, AwsLambdaOperations.findFunction(fqcn, projectName, stage, lambdaClient))
        }.map {
          case (handler, optionalInfo) =>
            val info = optionalInfo.fold(Console.YELLOW + "not yet deployed")(report)
            s"* ${Console.GREEN}${handler.lambdaConfig.simpleClassName}: ${Console.RESET}$info"
        }.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No DynamoDB handlers configured")
      }
      val scheduledEventHandlers: String = {
        config.lambdas.collect({ case h: ScheduledEventHandler => h }).map { handler =>
          val fqcn: String = handler.lambdaConfig.fqcn
          (handler, AwsLambdaOperations.findFunction(fqcn, projectName, stage, lambdaClient))
        }.map {
          case (handler, optionalInfo) =>
            val info = optionalInfo.fold(Console.YELLOW + "not yet deployed")(report)
            s"* ${Console.GREEN}${handler.lambdaConfig.simpleClassName}: ${Console.RESET}$info"
        }.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No scheduled event handlers configured")
      }
      val kinesisEventHandlers: String = {
        config.lambdas.collect({ case h: KinesisEventHandler => h }).map { handler =>
          val fqcn: String = handler.lambdaConfig.fqcn
          (handler, AwsLambdaOperations.findFunction(fqcn, projectName, stage, lambdaClient))
        }.map {
          case (handler, optionalInfo) =>
            val info = optionalInfo.fold(Console.YELLOW + "not yet deployed")(report)
            s"* ${Console.GREEN}${handler.lambdaConfig.simpleClassName}: ${Console.RESET}$info"
        }.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No kinesis event handlers configured")
      }
      val snsEventHandlers: String = {
        config.lambdas.collect({ case h: SNSEventHandler => h }).map { handler =>
          val fqcn: String = handler.lambdaConfig.fqcn
          (handler, AwsLambdaOperations.findFunction(fqcn, projectName, stage, lambdaClient))
        }.map {
          case (handler, optionalInfo) =>
            val info = optionalInfo.fold(Console.YELLOW + "not yet deployed")(report)
            s"* ${Console.GREEN}${handler.lambdaConfig.simpleClassName}: ${Console.RESET}$info"
        }.toNel.map(_.intercalate("\n")).getOrElse(Console.YELLOW + "No SNS event handlers configured")
      }
      s"""Lambdas:
        |Api Http Event Handlers:
        |$httpHandlers
        |DynamoDB Streams Handlers:
        |$dynamoHandlers
        |Scheduled Event Handlers:
        |$scheduledEventHandlers
        |Kinesis Event Handlers:
        |$kinesisEventHandlers
        |SNS Event Handlers:
        |$snsEventHandlers""".stripMargin
    }

    val endpointSummary: String = {
      samStack.flatMap(_.serviceEndpoint).map { endpoint =>
        config.lambdas.map {
          case HttpHandler(_, HttpConf(path, method, auth)) =>
            Console.GREEN + s"${method.toUpperCase} - ${endpoint.value}$path"
          case _ => ""
        }.toList.toNel.map(_.intercalate("\n")).getOrElse("No http handlers configured")
      }.getOrElse(Console.YELLOW + "No service endpoint found")
    }

    val report =
      s"""
        |====================
        |Stack State:
        |====================
        |$stackSummary
        |$lambdaSummary
        |DynamoDbTables:
        |$tablesSummary
        |SNS Topics:
        |$snsTopicsSummary
        |Kinesis Streams:
        |$kinesisStreamsSummary
        |Buckets:
        |$bucketsSummary
        |Endpoints:
        |$endpointSummary
      """.stripMargin

    log.info(report)
  }
}
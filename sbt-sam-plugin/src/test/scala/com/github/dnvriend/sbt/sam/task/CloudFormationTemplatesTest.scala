package com.github.dnvriend.sbt.sam.task

import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.sbt.aws.task.TemplateBody
import com.github.dnvriend.sbt.sam.generators.Generators
import com.github.dnvriend.sbt.sam.resource.ResourceOperations
import com.github.dnvriend.sbt.sam.resource.bucket.model.S3Bucket
import com.github.dnvriend.sbt.sam.resource.cognito.model.Authpool
import com.github.dnvriend.sbt.sam.resource.dynamodb.model.TableWithIndex
import com.github.dnvriend.sbt.sam.resource.firehose.s3.model.S3Firehose
import com.github.dnvriend.sbt.sam.resource.kinesis.model.KinesisStream
import com.github.dnvriend.sbt.sam.resource.role.model.IamRole
import com.github.dnvriend.sbt.sam.resource.sns.model.Topic
import com.github.dnvriend.test.TestSpec
import play.api.libs.json.{ JsValue, Json }

class CloudFormationTemplatesTest extends TestSpec with Generators with AllOps {
  it should "create a deployment bucket template" in {
    forAll { (pc: ProjectConfiguration) =>
      val template: TemplateBody = CloudFormationTemplates.deploymentBucketTemplate(pc)
      val deploymentBucketTemplate: JsValue = Json.parse(template.value)
      (deploymentBucketTemplate \ "AWSTemplateFormatVersion").as[String] shouldBe "2010-09-09"
      (deploymentBucketTemplate \ "Description").toOption shouldBe 'defined
      val resources = (deploymentBucketTemplate \ "Resources").as[Map[String, JsValue]]
      resources should not be 'empty
      resources.keys should contain("SbtSamDeploymentBucket")
      val deploymentBucket = resources("SbtSamDeploymentBucket")
      (deploymentBucket \ "Type").as[String] shouldBe "AWS::S3::Bucket"
      (deploymentBucket \ "Properties" \ "AccessControl").as[String] shouldBe "BucketOwnerFullControl"
      (deploymentBucket \ "Properties" \ "VersioningConfiguration" \ "Status").as[String] shouldBe "Enabled"
    }
  }

  it should "generate an s3 firehose project configuration" in {
    val jarName = "jarName"
    val latestVersion = "latestVersion"
    val stage = "dev"
    val accountId = "1234567890"
    val region = "eu-west-1"
    val projectName = "button-clicked-data-segment"
    val projectVersion = "1.0.0-SNAPSHOT"
    val projectDescription = "data segment for button clicks"
    val deploymentBucketName = "deployment-bucket"
    val cfTemplateName = "button-clicked-data-segment-cf-template"
    val prefixName = s"$projectName-$stage"
    val credsAndUser = iterCredentialsAndUser.next()
    val amazonUser = iterAmazonUser.next()

    val firehoseName = "button-clicked-firehose"
    val s3Firehose: S3Firehose = ResourceOperations
      .retrieveS3Firehose(
        s"""
          |s3firehoses {
          |   ButtonClickedFirehose {
          |    name = $firehoseName // A name for the delivery stream.
          |    compression = "UNCOMPRESSED" // UNCOMPRESSED | GZIP | ZIP | Snappy
          |    shard-count = 1 //
          |    retention-period-hours = 24 // min=24, max=168 (7 days)
          |    buffering-interval-in-seconds = 100 // min=60, max 900; default 300
          |    buffering-size = 1 // default 5, max = 128, min = 1
          |    export = true
          |  }
          |}
        """.stripMargin.tsc).head

    val samResources = SamResources(
      streams = Set(s3Firehose.stream(projectName, stage)),
      iamRoles = Set(s3Firehose.role(projectName, stage, accountId, region)),
      buckets = Set(s3Firehose.bucket(projectName, stage)),
      s3Firehoses = Set(s3Firehose)
    )

    val pc = ProjectConfiguration.fromConfig(
      projectName,
      projectVersion,
      projectDescription,
      deploymentBucketName,
      cfTemplateName,
      prefixName,
      stage,
      credsAndUser,
      amazonUser,
      samResources
    )

    val updateTemplate: TemplateBody = CloudFormationTemplates.updateTemplate(pc, jarName, latestVersion)
    val template: JsValue = Json.parse(updateTemplate.value)
    val templateJsonString = Json.prettyPrint(template)
    println(templateJsonString)
    val resources = (template \ "Resources").as[Map[String, JsValue]]
    (resources("ButtonClickedFirehose") \ "Properties" \ "DeliveryStreamName").as[String] shouldBe s"$projectName-$stage-$firehoseName"
    (resources("ButtonClickedFirehoseStream") \ "Properties" \ "Name").as[String] shouldBe s"$projectName-$stage-$firehoseName-stream"
    (resources("ButtonClickedFirehoseBucket") \ "Properties" \ "BucketName").as[String] shouldBe s"$projectName-$stage-$firehoseName-bucket"
    (resources("ButtonClickedFirehoseRole") \ "Properties" \ "RoleName").as[String] shouldBe s"$projectName-$stage-$firehoseName-role"
  }

  it should "generate an update template" in {
    val pc: ProjectConfiguration = iterProjectConfig.next()
    val httpHandler: HttpHandler = iterHttpHandler.next()
    val snsEventHandler: SNSEventHandler = iterSNSEventHandler.next()
    val scheduledEventHandler: ScheduledEventHandler = iterScheduledEventHandler.next()
    val kinesisEventHandler: KinesisEventHandler = iterKinesisEventHandler.next()
    val dynamoHandler: DynamoHandler = iterDynamoHandler.next()
    val stream: KinesisStream = iterKinesisStream.next()
    val s3Firehose: S3Firehose = iterS3Firehose.next()
    val table: TableWithIndex = iterTableWithIndex.next()
    val topic: Topic = iterTopic.next()
    val bucket: S3Bucket = iterS3Bucket.next()
    val role: IamRole = iterIamRole.next()
    val authPool: Authpool = iterAuthpool.next()
    val jarName = "jarName"
    val latestVersion = "latestVersion"
    val accountId = "0123456789"
    val region = "eu-west-1"
    val stage = pc.samStage.value
    val projectName = pc.projectName
    val conf = pc.copy(
      authpool = Option(authPool),
      streams = pc.streams :+ stream :+ s3Firehose.stream(projectName, stage),
      topics = pc.topics :+ topic,
      tables = pc.tables :+ table,
      lambdas = pc.lambdas ++ List(httpHandler, snsEventHandler, scheduledEventHandler, kinesisEventHandler, dynamoHandler),
      buckets = pc.buckets :+ bucket :+ s3Firehose.bucket(projectName, stage),
      s3Firehoses = pc.s3Firehoses :+ s3Firehose,
      iamRoles = pc.iamRoles :+ role :+ s3Firehose.role(projectName, stage, accountId, region)
    )
    val updateTemplate: TemplateBody = CloudFormationTemplates.updateTemplate(conf, jarName, latestVersion)
    val template: JsValue = Json.parse(updateTemplate.value)
    val templateJsonString = Json.prettyPrint(template)
    //    println(templateJsonString)
    (template \ "Resources").toOption shouldBe 'defined
    (template \ "Resources").asOpt[Map[String, JsValue]] shouldBe 'defined
    val resources = (template \ "Resources").as[Map[String, JsValue]]
  }
}

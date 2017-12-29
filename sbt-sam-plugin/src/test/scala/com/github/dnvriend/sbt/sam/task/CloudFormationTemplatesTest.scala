package com.github.dnvriend.sbt.sam.task

import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.sbt.aws.task.TemplateBody
import com.github.dnvriend.sbt.sam.generators.Generators
import com.github.dnvriend.sbt.sam.resource.dynamodb.model.TableWithIndex
import com.github.dnvriend.sbt.sam.resource.kinesis.model.KinesisStream
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

  it should "generate an update template" in {
    val pc: ProjectConfiguration = iterProjectConfig.next()
    val httpHandler: HttpHandler = iterHttpHandler.next()
    val snsEventHandler: SNSEventHandler = iterSNSEventHandler.next()
    val scheduledEventHandler: ScheduledEventHandler = iterScheduledEventHandler.next()
    val kinesisEventHandler: KinesisEventHandler = iterKinesisEventHandler.next()
    val dynamoHandler: DynamoHandler = iterDynamoHandler.next()
    val stream: KinesisStream = iterKinesisStream.next()
    val table: TableWithIndex = iterTableWithIndex.next()
    val topic: Topic = iterTopic.next()
    val jarName = "jarName"
    val latestVersion = "latestVersion"
    val conf = pc.copy(
      streams = List(stream),
      topics = List(topic),
      tables = List(table),
      lambdas = List(httpHandler, snsEventHandler, scheduledEventHandler, kinesisEventHandler, dynamoHandler)
    )
    val updateTemplate = CloudFormationTemplates.updateTemplate(conf, jarName, latestVersion)
    val template: JsValue = Json.parse(updateTemplate.value)
    (template \ "Resources").toOption shouldBe 'defined
    (template \ "Resources").asOpt[Map[String, JsValue]] shouldBe 'defined
    val resources = (template \ "Resources").as[Map[String, JsValue]]
    resources.keys.size shouldBe 10 // stream, topic, table, 5x handler + 1 bucket + api
  }
}

package com.github.dnvriend.sbt.sam.cf

import com.github.dnvriend.sbt.sam.cf.template.CloudFormationTemplate
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{ JsObject, JsValue, Json }

import scalaz.Scalaz._

object CloudFormation {
  /**
   * Generic utility that adds returns a 'Properties' object and accepts
   * JsValue of JsObject type to merge
   */
  def properties(props: JsValue*): JsObject = {
    Json.obj("Properties" -> props.toList.foldMap(identity)(JsMonoids.jsObjectMerge))
  }

  /**
   * The intrinsic function Ref returns the value of the specified parameter or resource.
   *
   * - When you specify a parameter's logical name, it returns the value of the parameter.
   * - When you specify a resource's logical name, it returns a value that you can typically
   * use to refer to that resource, such as a physical ID.
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

  /**
   * Substitutes variables in an input string with their associated values at runtime.
   * Variables can be template parameter names, resource logical IDs or resource attributes.
   */
  def subst(input: String): JsObject = Json.obj("Fn::Sub" -> input)

  /**
   * Returns the AWS account id eg '123456789012'
   */
  def accountId: JsObject = subst("${AWS::AccountId}")

  /**
   * Returns the AWS region eg: 'us-east-2'
   */
  def region: JsObject = subst("${AWS::Region}")

  /**
   * Returns the stack id eg: 'arn:aws:cloudformation:us-east-1:123456789012:stack/MyStack/1c2fa620-982a-11e3-aff7-50e2416294e0'
   */
  def stackId: JsObject = subst("${AWS::StackId}")

  /**
   * Returns the stack name eg: 'MyStack'
   */
  def stackName: JsObject = subst("${AWS::StackName}")

  /**
   * Returns the arn of an sns topic by means of subst
   */
  def snsArn(topicName: String): JsObject = {
    // arn:aws:sns:eu-west-1:123456789:sam-dynamodb-seed-dnvriend-person-received
    val snsInput = "arn:aws:sns:${AWS::Region}:${AWS::AccountId}:" + topicName
    subst(snsInput)
  }

  /**
   * Returns the arn of a kinesis stream by means of subst
   */
  def kinesisArn(streamName: String): JsObject = {
    // arn:aws:kinesis:eu-west-1:123456789:stream/sam-seed-test1-person-received
    val kinesisInput = "arn:aws:kinesis:${AWS::Region}:${AWS::AccountId}:stream/" + streamName
    subst(kinesisInput)
  }

  /**
   * Returns the arn of a kinesis stream by means of accountId and Region
   */
  def kinesisArn(accountId: String, region: String, streamName: String): String = {
    s"arn:aws:kinesis:$region:$accountId:stream/$streamName"
  }

  /**
   * Returns the arn of a kinesis data firehose delivery stream
   */
  def firehoseDeliveryStreamArn(deliveryStreamName: String): JsObject = {
    //  arn:aws:firehose:us-east-2:123456789012:deliverystream/delivery-stream-name
    val firehoseDeliveryStreamInput = "arn:aws:firehose:${AWS::Region}:${AWS::AccountId}:deliverystream/" + deliveryStreamName
    subst(firehoseDeliveryStreamInput)
  }

  /**
   * Returns the arn of a role
   */
  def roleArn(accountId: String, roleName: String): String = {
    s"arn:aws:iam::$accountId:role/$roleName"
  }

  /**
   * Returns the arn of an s3 bucket
   */
  def bucketArn(bucketName: String): String = {
    s"arn:aws:s3:::$bucketName"
  }
}
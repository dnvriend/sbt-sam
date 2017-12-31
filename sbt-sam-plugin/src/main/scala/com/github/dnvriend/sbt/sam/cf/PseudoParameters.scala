package com.github.dnvriend.sbt.sam.cf

import play.api.libs.json.{ JsObject, Json }

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
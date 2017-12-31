package com.github.dnvriend.sbt.sam.cf.template

import play.api.libs.json.{ Json, Writes }

object Transform {
  implicit val writes: Writes[Transform] = Writes.apply(model => {
    import model._
    Json.obj("Transform" -> value)
  })

  def samTransform: Transform = Transform("AWS::Serverless-2016-10-31")
}

/**
 * The optional Transform section specifies one or more transforms that AWS CloudFormation uses to process your template.
 * The Transform section builds on the simple, declarative language of AWS CloudFormation with a powerful macro system.
 *
 * For serverless applications, specifies the version of the AWS Serverless Application Model (AWS SAM) to use.
 * When you specify a transform, you can use AWS SAM syntax to declare resources in your template. The model defines
 * the syntax that you can use and how it is processed.
 *
 * AWS CloudFormation supports AWS::Serverless and AWS::Include transform types:
 * - An 'AWS::Serverless' transform specifies the version of the AWS Serverless Application Model (AWS SAM) to use.
 *   This model defines the AWS SAM syntax that you can use and how AWS CloudFormation processes it. When you create a
 *   change set, AWS CloudFormation resolves all Transform functions.
 * - An 'AWS::Include' transform works with template snippets that are stored separately from the main AWS CloudFormation
 *   template. You can insert these snippets into your main template when Creating a Change Set
 *   or Updating Stacks Using Change Sets.
 */
case class Transform(value: String = "AWS::Serverless-2016-10-31")


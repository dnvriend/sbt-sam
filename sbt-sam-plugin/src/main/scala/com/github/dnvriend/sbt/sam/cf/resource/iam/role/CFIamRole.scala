package com.github.dnvriend.sbt.sam.cf.resource.iam.role


import com.github.dnvriend.sbt.sam.cf.resource.Resource
import play.api.libs.json.{JsValue, Json, Writes}

object CFS3IamPolicy {
  implicit val writes: Writes[CFS3IamPolicy] = Writes.apply(model => {
    import model._
    Json.obj(
      "PolicyName" -> policyName,
      "PolicyDocument" -> policyDocument
    )
  })

  /**
    * Services, eg. Kinesis firehose, need access to several resources like S3, Kinesis, Log groups etc.
    * To get access to these resources, Firehose needs (temporary) credentials and gets it by assuming a role.
    * Firehose thus needs a role, and that role defines the resources that the role has access to such as S3, Kinesis,
    * Log groups etc
    * Firehose needs a trust policy document in order to assume the role that is attached to the role. In order to assume
    * the role, Firehose needs to be granted the capability to assume the role by setting the accountId, thus has
    * been granted to assume the role by this accountId.
    */
  def assumeRolePolicyDocument(principal: String, accountId: String): JsValue = {
    Json.obj(
      "Version" -> "2012-10-17",
      "Statement" -> Json.arr(
        Json.obj(
          "Effect" -> "Allow",
          "Principal" -> Json.obj("Service" -> principal),
          "Action" -> "sts:AssumeRole",
          "Condition" -> Json.obj(
            "StringEquals" -> Json.obj(
              "sts:ExternalId" -> accountId
            )
          )
        )
      )
    )
  }

  /**
    * A Policy describes what actions are allowed on what resources.
    */
  def allowAccessPolicyDocument(actions: List[String], resources: List[String]): JsValue = {
    Json.obj(
      "Version" -> "2012-10-17",
      "Statement" -> Json.arr(
        Json.obj(
          "Effect"-> "Allow",
          "Action" -> actions,
          "Resource" -> resources
        )
      )
    )
  }
}

/**
  * A Policy describes what actions are allowed on what resources.
  */
case class CFS3IamPolicy(
                               /**
                                 * The name of the policy.
                                 */
                               policyName: String,

                               /**
                                 * A policy document that describes what actions are allowed on which resources.
                                 */
                               policyDocument: JsValue)


object CFIamRole {
  implicit val writes: Writes[CFIamRole] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::IAM::Role",
        "Properties" -> Json.obj(
          "RoleName" -> roleName,
          "AssumeRolePolicyDocument" -> assumeRolePolicyDocument,
          "ManagedPolicyArns" -> managedPolicyArns,
          "Policies" -> policies
        )
      )
    )
  })
}

/**
  * An IAM role is similar to a user, in that it is an AWS identity
  * with permission policies that determine what the
  * identity can, and cannot do in AWS.
  *
  * However, instead of being uniquely associated with one person,
  * a role is intended to be assumable by anyone who needs it.
  *
  * Also, a role does not have standard long-term credentials
  * (password or access keys) associated with it. Instead, if a user assumes a role,
  * temporary security credentials are created dynamically and provided to the user.
  */
case class CFIamRole(
                      /**
                        *
                        */
                      logicalName: String,

                      /**
                        * A name for the IAM role.
                        *
                        * If you specify a name, you must specify the CAPABILITY_NAMED_IAM value
                        * to acknowledge your template's capabilities.
                        *
                        * see: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-iam-template.html#using-iam-capabilities
                        */
                      roleName: String,

                      /**
                        * One or more managed policy ARNs to attach to this role.
                        */
                      managedPolicyArns: List[String],

                      /**
                        * The policies to associate with this role
                        */
                      policies: List[CFS3IamPolicy],

                      /**
                        * The trust policy that is associated with this role.
                        * You can associate only one assume role policy with a role.
                        */
                      assumeRolePolicyDocument: JsValue,
                    ) extends Resource
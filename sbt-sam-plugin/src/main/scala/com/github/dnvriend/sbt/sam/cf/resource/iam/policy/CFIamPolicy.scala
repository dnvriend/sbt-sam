package com.github.dnvriend.sbt.sam.cf.resource.iam.policy

import play.api.libs.json.{JsValue, Json, Writes}

object CFIamPolicy {
  implicit val writes: Writes[CFIamPolicy] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Properties" -> Json.obj(
          "PolicyName" ->  policyName,
          "Users" -> users,
          "Roles"  -> roles,
          "Groups" -> groups,
          "PolicyDocument" -> policyDocument
        )
      )
    )
  })
}

/**
  * A policy is an entity in AWS that, when attached to an identity or resource, defines their permissions.
  *
  * AWS evaluates these policies when a principal, such as a user, makes a request. Permissions in the policies
  * determine whether the request is allowed or denied.
  *
  * Policies are stored in AWS as JSON documents attached to:
  * - principals as "identity-based policies": policies that you can attach to a principal (or identity),
  *   such as an IAM user, role, or group. These policies control what actions that identity can perform,
  *   on which resources, and under what conditions.
  * - resources as "resource-based policies": policy documents that you attach to a resource such as an Amazon S3 bucket.
  *   These policies control what actions a specified principal can perform on that resource and under what conditions.
  *   Resource-based policies are inline policies, and there are no managed resource-based policies.
  *
  * The 'AWS::IAM::Policy' resource associates an IAM policy (document) with IAM users, roles, or groups.
  */
case class CFIamPolicy(
                      logicalName: String,

                      /**
                        * The name of the policy.
                        */
                      policyName: String,

                      /**
                        * The names of users for whom you want to add the policy.
                        */
                      users: List[String],

                      /**
                        * The names of AWS::IAM::Roles to which this policy will be attached.
                        */
                      roles: List[String],

                      /**
                        * The names of groups to which you want to add the policy.
                        */
                      groups: List[String],

                      /**
                        * A policy document that contains permissions to add to the specified users or groups.
                        */
                      policyDocument: JsValue,
                      )
package com.github.dnvriend.sbt.sam.cf.resource.iam.policy

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{JsValue, Json, Writes}

import scalaz.syntax.all._
import scalaz.NonEmptyList

object CFIamRole {
  implicit val writes: Writes[CFIamRole] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::IAM::Role",
        "Properties" -> NonEmptyList(
          Json.obj("RoleName" -> roleName),
          Json.obj("AssumeRolePolicyDocument" -> assumeRolePolicyDocument),
          Json.obj("ManagedPolicyArns" -> managedPolicyArns),
        ).widen[JsValue].foldMap(identity)(JsMonoids.jsObjectMerge)
      )
    )
  })
}
case class CFIamRole(
                      /**
                        * Logical name for the role within the CF template
                        */
                      logicalName: String,

                      /**
                        * A name for the IAM role.
                        */
                      roleName: String,

                     /**
                       * Type: A JSON policy document
                       * AWS Identity and Access Management (IAM) requires that policies be in JSON format.
                       */
                      assumeRolePolicyDocument: CFIamPolicyDocument,

                     /**
                       * One or more managed policy ARNs to attach to this role.
                       */
                     managedPolicyArns: List[String],

                    ) extends Resource

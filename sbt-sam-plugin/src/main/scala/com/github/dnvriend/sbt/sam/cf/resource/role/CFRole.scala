package com.github.dnvriend.sbt.sam.cf.resource.role

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{JsValue, Json, Writes}

import scalaz._
import scalaz.Scalaz._

object CFPolicy {
  implicit val writes: Writes[CFPolicy] = Writes.apply(model => {
    import model._
    Json.obj()
  })
}

case class CFPolicy()

object CFRoleName {
  implicit val writes: Writes[CFRoleName] = Writes.apply(model => {
    import model._
    Json.obj("RoleName" -> roleName)
  })
}

case class CFRoleName(roleName: String)

object CFRole {
  implicit val writes: Writes[CFRole] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::IAM::Role",
        "Properties" -> NonEmptyList(
          Json.obj(),
          Json.obj(),
        ).widen[JsValue].foldMap(identity)(JsMonoids.jsObjectMerge)
      )
    )
  })
}

case class CFRole(logicalName: String,
                  roleName: CFRoleName,
                  policyDocument: JsValue,
                  managedPolicyArns: List[String],
                  policies: List[CFPolicy],
                 )

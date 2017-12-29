package com.github.dnvriend.sbt.sam.cf.resource.policy

import play.api.libs.json.{ Json, Writes }

case object IamRole {
  implicit val writes: Writes[IamRole] = Writes.apply(model => {
    import model._
    Json.obj("Ref" -> ref)
  })
}
case class IamRole(ref: String)

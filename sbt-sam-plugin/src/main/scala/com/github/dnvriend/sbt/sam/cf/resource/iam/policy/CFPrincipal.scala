package com.github.dnvriend.sbt.sam.cf.resource.iam.policy

import play.api.libs.json.{ Json, Writes }

object CFPrincipal {
  implicit val writes: Writes[CFPrincipal] = Writes.apply(model => {
    import model._
    Json.obj("Principal" -> Json.obj(
      "Service" -> service
    ))
  })
}

case class CFPrincipal(service: String)

package com.github.dnvriend.sbt.sam.cf.resource.policy

import play.api.libs.json.{ Json, Writes }

object IamStatement {
  implicit val writes: Writes[IamStatement] = Writes.apply(model => {
    import model._
    Json.obj(
      "Effect" -> "Allow",
      "Action" -> allowedActions,
      "Resource" -> resource
    )
  })
}
case class IamStatement(allowedActions: List[String], resource: String)

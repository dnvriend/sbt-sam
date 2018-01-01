package com.github.dnvriend.sbt.sam.cf.resource.iam.policy

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{ JsValue, Json, Writes }

import scalaz.syntax.all._
import scalaz.NonEmptyList

object CFIamStatement {
  implicit val writes: Writes[CFIamStatement] = Writes.apply(model => {
    import model._
    NonEmptyList(
      Json.obj("Effect" -> effect),
      Json.toJson(principal),
      Json.obj("Action" -> action),
      Json.toJson(condition),
      //Json.toJson(resource.map(value => Json.obj("Resource" -> value)))
    ).foldMap(identity)(JsMonoids.jsObjectMerge)
  })

  def allowAssumeRole(principal: CFPrincipal, accountId: String): CFIamStatement = {
    CFIamStatement(
      "Allow",
      "sts:AssumeRole",
      Option(principal),
      // This is so that only you can request Firehose to assume the IAM role.
      // see: https://forums.aws.amazon.com/thread.jspa?threadID=221340
      Option(CFIamStatementCondition(Option(CFIamStatementConditionStringEquals("sts:ExternalId", accountId)))),
      None)
  }
}
case class CFIamStatement(
    effect: String,
    action: String,
    principal: Option[CFPrincipal],
    condition: Option[CFIamStatementCondition],
    resource: Option[String])

object CFIamStatementCondition {
  implicit val writes: Writes[CFIamStatementCondition] = Writes.apply(model => {
    import model._
    Json.obj(
      "Condition" -> Json.toJson(stringEquals)
    )
  })
}

case class CFIamStatementCondition(stringEquals: Option[CFIamStatementConditionStringEquals])

object CFIamStatementConditionStringEquals {
  implicit val writes: Writes[CFIamStatementConditionStringEquals] = Writes.apply(model => {
    import model._
    Json.obj(
      "StringEquals" -> Json.obj(
        key -> value
      ))
  })
}

case class CFIamStatementConditionStringEquals(key: String, value: String)
package com.github.dnvriend.sbt.sam.cf.resource.iam.policy

import play.api.libs.json.{ Json, Writes }

/**
 * The trust policy that is associated with this role.
 * You can associate only one assume role policy with a role.
 */
object CFIamPolicyDocument {
  implicit val writes: Writes[CFIamPolicyDocument] = Writes.apply(model => {
    import model._
    Json.obj(
      "Version" -> "2012-10-17",
      "Statement" -> Json.toJson(statements)
    )
  })
}
case class CFIamPolicyDocument(statements: List[CFIamStatement])
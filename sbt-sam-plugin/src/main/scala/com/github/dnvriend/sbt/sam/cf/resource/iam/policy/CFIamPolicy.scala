package com.github.dnvriend.sbt.sam.cf.resource.iam.policy

import play.api.libs.json.{Json, Writes}

object CFIamPolicy {
  implicit val writes: Writes[CFIamPolicy] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalResourceId -> Json.obj(
        "Type" -> "AWS::IAM::Policy",
        "Properties" -> Json.obj(
          "PolicyName" -> policyName,
          "PolicyDocument" -> Json.obj(
            "Version" -> "2012-10-17",
            "Statement" -> Json.toJson(statements),
//            "Roles" -> Json.toJson(roles),
          )
        )
      )
    )
  })
}

case class CFIamPolicy(
                        logicalResourceId: String,
                        policyName: String,
                        statements: List[CFIamStatement],
                        roles: List[CFIamRole],
                    )

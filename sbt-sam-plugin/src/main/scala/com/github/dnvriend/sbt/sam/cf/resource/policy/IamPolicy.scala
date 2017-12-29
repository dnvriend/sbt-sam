package com.github.dnvriend.sbt.sam.cf.resource.policy

import play.api.libs.json.{Json, Writes}

object IamPolicy {
  implicit val writes: Writes[IamPolicy] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalResourceId -> Json.obj(
        "Type" -> "AWS::IAM::Policy",
        "DependsOn" -> dependsOnLogicalResourceId,
        "Properties" -> Json.obj(
          "PolicyName" -> policyName,
          "PolicyDocument" -> Json.obj(
            "Version" -> "2012-10-17",
            "Statement" -> Json.toJson(statements),
            "Roles" -> Json.toJson(roles),
          )
        )
      )
    )
  })
}

case class IamPolicy(
                      logicalResourceId: String,
                      dependsOnLogicalResourceId: String,
                      policyName: String,
                      statements: List[IamStatement],
                      roles: List[IamRole],
                    )

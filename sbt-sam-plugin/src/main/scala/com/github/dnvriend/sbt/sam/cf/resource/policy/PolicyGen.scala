package com.github.dnvriend.sbt.sam.cf.resource.policy

object PolicyGen {
  //  private def parsePolicies(policies: Set[Policy]): JsObject = {
  //    policies.foldMap { policy ⇒
  //      Json.obj(
  //        policy.configName → Json.obj(
  //          "Type" → "AWS::IAM::Policy",
  //          "DependsOn" → policy.dependsOn,
  //          "Properties" → Json.obj(
  //            "PolicyName" → policy.properties.name,
  //            "PolicyDocument" → Json.obj(
  //              "Version" → "2012-10-17",
  //              "Statement" → statementsToJson(policy.properties.statements)),
  //            "Roles" → rolesToJson(policy.properties.roles)
  //          )
  //        )
  //      )
  //    }
  //  }

  //  private def statementsToJson(statements: List[Statements]): JsArray = {
  //    statements.map { statement ⇒
  //      Json.obj(
  //        "Effect" → "Allow",
  //        "Action" → statement.allowedActions,
  //        "Resource" → statement.resource
  //      )
  //    }.foldLeft(Json.arr())((arr, a) ⇒ arr ++ Json.arr(a))
  //  }
  //
  //  private def rolesToJson(roles: List[Role]): JsArray = {
  //    roles.map { role ⇒
  //      Json.obj("Ref" → role.ref)
  //    }.foldLeft(Json.arr())((arr, a) ⇒ arr ++ Json.arr(a))
  //  }
}

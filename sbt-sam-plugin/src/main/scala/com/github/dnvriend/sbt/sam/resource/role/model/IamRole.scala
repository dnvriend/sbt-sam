package com.github.dnvriend.sbt.sam.resource.role.model

object IamPolicyAllow {
  final val AllowAllActionsToAllResources: IamPolicyAllow = {
    IamPolicyAllow("AllowAllActionsToAllResources", List("*"), List("*"))
  }
}

case class IamPolicyAllow(
                    name: String,
                    actions: List[String],
                    resources: List[String]
                    )

case class IamRole(
                    name: String,
                    configName: String = "",
                    allowAssumeRolePrincipal: String,
                    managedPolicyArns: List[String],
                    allow: List[IamPolicyAllow],
                  )

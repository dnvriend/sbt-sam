package com.github.dnvriend.sbt.sam.resource.role.model

case class IamRole(
                    name: String,
                    configName: String = "",
                    principalServiceName: String,
                    managedPolicyArns: List[String],
                    export: Boolean = false,
                  )

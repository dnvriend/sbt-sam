package com.github.dnvriend.sbt.sam.cf.resource.statemachine

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.resource.Resource
import play.api.libs.json.{ JsObject, JsValue, Json, Writes }

object StateMachine {
  private def statesExecutionRole(logicalName: String): JsValue = Json.parse(
    s"""
      |{
      |  "${logicalName}StatesExecutionRole": {
      |    "Type": "AWS::IAM::Role",
      |    "Properties": {
      |      "AssumeRolePolicyDocument": {
      |        "Version": "2012-10-17",
      |        "Statement": [
      |          {
      |            "Effect": "Allow",
      |            "Principal": {
      |              "Service": [
      |                "states.amazonaws.com"
      |              ]
      |            },
      |            "Action": "sts:AssumeRole"
      |          }
      |        ]
      |      },
      |      "Path": "/",
      |      "Policies": [
      |        {
      |          "PolicyName": "${logicalName}StatesExecutionPolicy",
      |          "PolicyDocument": {
      |            "Version": "2012-10-17",
      |            "Statement": [
      |              {
      |                "Effect": "Allow",
      |                "Action": [
      |                  "lambda:InvokeFunction"
      |                ],
      |                "Resource": "*"
      |              }
      |            ]
      |          }
      |        }
      |      ]
      |    }
      |  }
      |}
    """.stripMargin
  )

  implicit val writes: Writes[StateMachine] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::StepFunctions::StateMachine",
        "Properties" -> Json.obj(
          "StateMachineName" -> stateMachineName,
          "DefinitionString" -> definitionString,
          "RoleArn" -> CloudFormation.getAtt(logicalName + "StatesExecutionRole", "Arn")
        )
      )
    ) ++ statesExecutionRole(logicalName).as[JsObject]
  })
}

case class StateMachine(
    logicalName: String,
    stateMachineName: String,
    definitionString: JsValue
) extends Resource

package com.github.dnvriend.sbt.sam.task

import play.api.libs.json.Reads.of
import play.api.libs.json._

case class DiscoveredStateMachine(
    name: String,
    stateMachineDefinition: String,
    taskLogicalResourceId: Set[String]
)

object DiscoverStateMachines {
  def run(lambdas: Set[LambdaHandler]): List[DiscoveredStateMachine] = {
    lambdas
      .collect({ case l: StepFunctionTaskHandler => l })
      .groupBy(_.stepFunctionTaskConf.stateMachine)
      .map({
        case (stateMachineClass, tasks) => getStateMachineName(stateMachineClass) -> getStateMachineDefinition(stateMachineClass) -> tasks
      })
      .mapValues(_.map(task => (task.stepFunctionTaskConf.stateName, task.lambdaConfig.simpleClassName)))
      .map({
        case ((name, stateMachineDefinition), stateNameVsLogicalResourceNames) =>
          val stateMachineDefinitionJsValue = Json.parse(stateMachineDefinition)
          val jsonTransformer = (__ \ "States").json.pickBranch(createJsonTransformer(stateNameVsLogicalResourceNames))
          val statesTransformed = stateMachineDefinitionJsValue.transform(jsonTransformer).get
          DiscoveredStateMachine(name, Json.prettyPrint(stateMachineDefinitionJsValue.as[JsObject] ++ statesTransformed), stateNameVsLogicalResourceNames.map(_._2))
      }).toList
  }

  def getStateMachineDefinition(stateMachineClass: Class[_]): String = {
    val factoryMethod = stateMachineClass.getMethod("apply")
    val stateMachineFactory = stateMachineClass.newInstance()
    factoryMethod.invoke(stateMachineFactory).asInstanceOf[String]
  }

  def getStateMachineName(stateMachineClass: Class[_]): String = {
    val factoryMethod = stateMachineClass.getMethod("name")
    val stateMachineFactory = stateMachineClass.newInstance()
    factoryMethod.invoke(stateMachineFactory).asInstanceOf[String]
  }

  def createJsonTransformer(stateNamesVsLogicalResourceNames: Set[(String, String)]): Reads[JsObject] = {
    stateNamesVsLogicalResourceNames.map {
      case (stateName, logicalResourceName) =>
        (__ \ stateName \ "Resource").json.update(
          of[JsString].map { case JsString(resource) => JsString("${" + logicalResourceName + "}") }
        )
    }.reduce(_ andThen _)
  }
}

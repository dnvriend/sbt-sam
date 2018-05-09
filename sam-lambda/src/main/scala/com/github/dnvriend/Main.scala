//package com.github.dnvriend
//
//import com.amazonaws.services.stepfunctions.builder.StateMachine
//import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder._
//import play.api.libs.json.Json
//import play.api.libs.json._
//import play.api.libs.json.Reads._
//
//class MyStateMachine {
//  def apply(): StateMachine = {
//    stateMachine()
//      .comment("")
//      .startAt("Hello")
//      .state("Hello", taskState()
//        .resource("None")
//        .transition(next("Bye"))
//      )
//      .state("Bye", taskState()
//        .resource("None")
//        .transition(end())
//      )
//      .build()
//  }
//}
//
//object Main extends App {
//  val c = Class.forName("com.github.dnvriend.MyStateMachine")
//  val method = c.getMethod("apply")
//  val o = c.newInstance()
//  method.invoke(o) match {
//    case sm: StateMachine =>
//      val jsString = sm.toPrettyJson
//      println(jsString)
//
//      val stateNameVsTaskLambdaName = Map(
//        "Hello" -> "HelloLambdaTask",
//        "Bye" -> "ByeLambdaTask"
//      )
//
//      val resFold = stateNameVsTaskLambdaName.map {
//        case (stateName, lambdaName) =>
//          (__ \ stateName \ "Resource").json.update(
//            of[JsString].map { case JsString(resource) => JsString("${" + lambdaName + "}") }
//          )
//      }.reduce(_ andThen _)
//
//      val resFold2 = (__ \ "States").json.pickBranch(resFold)
//
//      //      val jsonTransformer = (__ \ "States").json.pickBranch(
//      //        (__ \ "Hello" \ "Resource").json.update(
//      //          of[JsString].map { case JsString(resource) => JsString(s"foo") }
//      //        ) andThen
//      //          (__ \ "Bye" \ "Resource").json.update(
//      //            of[JsString].map { case JsString(resource) => JsString("bar") }
//      //          )
//      //      )
//
//      val json = Json.parse(jsString)
//      val transformed = json.transform(resFold2).get
//
//      val res = json.as[JsObject] ++ transformed
//      println(Json.prettyPrint(res))
//  }
//}

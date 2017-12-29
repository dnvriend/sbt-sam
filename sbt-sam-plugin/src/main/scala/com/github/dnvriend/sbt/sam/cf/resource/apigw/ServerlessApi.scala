package com.github.dnvriend.sbt.sam.cf.resource.apigw

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.sam.cf.resource.apigw.swagger.Swagger
import com.github.dnvriend.sbt.sam.task.HttpHandler
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json._

import scalaz.NonEmptyList
import scalaz.Scalaz._

object ServerlessApiSwaggerDefinitionBody {
  implicit val writes: Writes[ServerlessApiSwaggerDefinitionBody] = Writes.apply(model => {
    import model._
    Json.obj("DefinitionBody" -> Swagger.spec(projectName, stage, httpHandlers))
  })
}

case class ServerlessApiSwaggerDefinitionBody(
    projectName: String,
    stage: String,
    httpHandlers: List[HttpHandler])

object ServerlessApiStageName {
  implicit val writes: Writes[ServerlessApiStageName] = Writes.apply(model => {
    import model._
    Json.obj("StageName" -> stageName)
  })
}
case class ServerlessApiStageName(stageName: String)

object ServerlessApiProperties {
  implicit val writes: Writes[ServerlessApiProperties] = Writes.apply(model => {
    import model._
    NonEmptyList(
      Json.toJson(stageName),
      Json.toJson(swaggerDefinitionBody)
    ).foldMap(identity)(JsMonoids.jsObjectMerge)
  })
}
case class ServerlessApiProperties(
    stageName: ServerlessApiStageName,
    swaggerDefinitionBody: ServerlessApiSwaggerDefinitionBody
)

object ServerlessApi {
  implicit val writes: Writes[ServerlessApi] = Writes.apply(model => {
    import model._
    Json.obj(
      "ServerlessRestApi" -> Json.obj(
        "Type" -> "AWS::Serverless::Api",
        "Properties" -> Json.toJson(properties)
      )
    )
  })
}
case class ServerlessApi(properties: ServerlessApiProperties) extends Resource

//  /**
//    * Returns the logical resource id
//    */
//  def logicalResourceId(config: ProjectConfiguration): String = {
//    "ServerlessRestApi"
//  }
//
//  /**
//    * Returns the logicalId of 'AWS::ApiGateway::RestApi'
//    */
//  def logicalIdRestApi(config: ProjectConfiguration): JsObject = {
//    CloudFormation.ref(logicalResourceId(config))
//  }
//
//  /**
//    * Returns the logicalId of 'AWS::ApiGateway::Stage'
//    */
//  def logicalIdStage(config: ProjectConfiguration): JsObject = {
//    val logicalName = s"${logicalResourceId(config)}${config.samStage.value}Stage"
//    CloudFormation.ref(logicalName)
//  }
//
//  private def properties(props: JsValue*): JsObject = {
//    Json.obj("Properties" -> props.toList.foldMap(identity)(JsMonoids.jsObjectMerge))
//  }
//
//  def resource(config: ProjectConfiguration): JsValue = {
//    if (!config.lambdas.exists(_.isInstanceOf[HttpHandler])) JsNull else {
//      Json.obj(
//        logicalResourceId(config) -> (Json.obj(
//          "Type" -> "AWS::Serverless::Api",
//          //          "DependsOn" -> Cognito.UserPool.logicalResourceId(config),
//        ) ++ properties(
//          propStageName(config),
//          propDefinitionBody(config)
//        ))
//      )
//    }
//  }
//
//  def propDefinitionBody(config: ProjectConfiguration): JsValue = {
//    Json.obj("DefinitionBody" -> Swagger.spec(config))
//  }
//
//  def definitionBodyElements(xs: JsValue*): JsValue = {
//    xs.toList.foldMap(identity)(JsMonoids.jsObjectMerge)
//  }
//}
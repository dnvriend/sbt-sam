package com.github.dnvriend.sbt.sam.cf.resource.apigw.swagger

import com.github.dnvriend.sbt.sam.task.{HttpHandler, ProjectConfiguration}
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{JsValue, Json}

import scalaz.Scalaz._

/**
  * see: https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md
  * see: http://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-swagger-extensions.html
  */
object Swagger {
  private def merge(parts: JsValue*): JsValue = {
    parts.toList.foldMap(identity)(JsMonoids.jsObjectMerge)
  }
  def spec(projectName: String, stage: String, httpHandlers: List[HttpHandler]): JsValue = {
    merge(
      Parts.swaggerVersion,
      Parts.info(projectName, stage),
      Parts.paths(httpHandlers),
      //      Parts.securityDefinitions(config),
    )
  }

  object Parts {
    /**
      * Required: Specifies the Swagger Specification version being used. The value must be '2.0'
      */
    val swaggerVersion: JsValue = Json.obj("swagger" -> "2.0")

    /**
      * Required: Provides metadata about the API. The metadata can be used by the clients if needed.
      */
    def info(projectName: String, stage: String): JsValue = {
      val title = s"$projectName-$stage"
      Json.obj(
        "info" -> Json.obj(
          "version" -> "2017-02-24T04:09:00Z",
          "title" -> title
        )
      )
    }

    /**
      * Required: The available paths and operations for the API.
      */
    def paths(httpHandlers: List[HttpHandler]): JsValue = {
      val handersByPath: Map[String, List[HttpHandler]] = httpHandlers.groupBy(_.httpConf.path)
      val pathsWithOperations = handersByPath.map { case (resourcePath, handlers) => path(resourcePath, handlers) }.toList
      Json.obj("paths" -> pathsWithOperations.foldMap(identity)(JsMonoids.jsObjectMerge))
    }

    /**
      * A relative path to an individual endpoint. The field name MUST begin with a slash.
      * The path is appended to the basePath in order to construct the full URL.
      */
    def path(path: String, handlersForPath: List[HttpHandler]): JsValue = {
      val operations = handlersForPath.map(handler => operation(handler))
      Json.obj(path -> merge(operations:_*))
    }

    /**
      * add swagger operation and AWS ApiGateway extensions
      */
    def operation(handler: HttpHandler) = {
      val method: String = handler.httpConf.method
      Json.obj(method -> merge(
        AmazonApiGatewayIntegration.swaggerExtension(handler),
        responses,
        //          security,
      )
      )
    }

    /**
      * Security scheme definitions that can be used across the specification.
      * http://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-integrate-with-cognito.html
      * http://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-enable-cognito-user-pool.html
      * https://github.com/dnvriend/serverless-test/blob/master/05-person-repository/auth.txt
      * https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#security-definitions-object
      * http://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-swagger-extensions-authorizer.html
      */
    def securityDefinitions(config: ProjectConfiguration): JsValue = {
      Json.obj(
        "securityDefinitions" -> Json.obj(
          "auth_pool" -> Json.obj(
            "type" -> "apiKey",
            "name" -> "Authorization",
            "in" -> "header",
            "x-amazon-apigateway-authtype" -> "cognito_user_pools",
            "x-amazon-apigateway-authorizer" -> Json.obj(
              "type" -> "cognito_user_pools",
              "providerARNs" -> Json.arr(
                "arn:aws:cognito-idp:eu-west-1:015242279314:userpool/eu-west-1_V0UvYaYoz" //todo: replace hardcoded cognito arn with the one configured in the project
              ),
            )
          )
        )
      )
    }

    /**
      * A list of MIME types the APIs can consume.
      */
    val consumes = Json.obj("consumes" -> Json.arr("application/json"))
    /**
      * A list of MIME types the APIs can produce.
      */
    val produces = Json.obj("produces" -> Json.arr("application/json"))

    /**
      * An object to hold responses that can be used across operations.
      */
    val responses = Json.obj("responses" -> Json.obj())

    /**
      * A declaration of which security schemes are applied for the API as a whole.
      */
    val security = Json.obj("security" -> Json.arr(Json.obj("auth_pool" -> Json.arr())))
  } // end parts

  /**
    * Specifies details of the backend integration used for this method.
    * The result is an API Gateway integration object. In this case, it
    * uses an AWS Lambda function as the backend for the request.
    */
  object AmazonApiGatewayIntegration {
    def swaggerExtension(http: HttpHandler): JsValue = {
      Json.obj(
        "x-amazon-apigateway-integration" -> merge(
          uri(http),
          passthroughBehavior,
          httpMethod,
          integrationType,
        )
      )
    }

    /**
      * The HTTP method used in the integration request. For Lambda function invocations, the value must be POST.
      */
    val httpMethod: JsValue = Json.obj("httpMethod" -> "POST")
    /**
      * HTTP, HTTP_PROXY, AWS, AWS_PROXY
      * see: https://docs.aws.amazon.com/apigateway/api-reference/resource/integration/
      */
    val integrationType: JsValue = Json.obj("type" -> "aws_proxy")
    val passthroughBehavior: JsValue = Json.obj("passthroughBehavior" -> "when_no_match")

    def uri(http: HttpHandler): JsValue = {
      val lambdaUri = "arn:aws:apigateway:${AWS::Region}:lambda:path/2015-03-31/functions/${" + http.lambdaConfig.simpleClassName + ".Arn}/invocations"
      Json.obj("uri" -> Json.obj("Fn::Sub" -> lambdaUri))
    }
  }
}
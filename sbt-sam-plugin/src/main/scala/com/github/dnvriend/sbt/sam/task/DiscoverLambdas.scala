package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.lambda.runtime.RequestStreamHandler

import scalaz.Show

final case class ProjectLambda(projectClass: ProjectClass)

object DiscoverLambdas {
  def lambdaPredicate(pc: ProjectClass): Boolean = {
    val superClassName = Option(pc.cl.getSuperclass).map(_.getName).getOrElse("")
    interfacesContainsRequestStreamHandlerInterface(pc.interfaces) ||
      List(
        "com.github.dnvriend.lambda.BinaryDynamoRepoApiGatewayHandler",
        "com.github.dnvriend.lambda.JsonDynamoRepoApiGatewayHandler",
        "com.github.dnvriend.sam.akka.stream.JsonApiGatewayHandler",
        "com.github.dnvriend.lambda.JsonApiGatewayHandler",
        "com.github.dnvriend.lambda.JsonDApiGatewayHandler",
        "com.github.dnvriend.lambda.ApiGatewayHandler",
        "com.github.dnvriend.lambda.DynamoDBHandler",
        "com.github.dnvriend.sam.akka.stream.KinesisEventProcessor",
        "com.github.dnvriend.sam.akka.stream.KinesisEventHandler",
        "com.github.dnvriend.lambda.KinesisEventHandler",
        "com.github.dnvriend.lambda.ScheduledEventHandler",
        "com.github.dnvriend.lambda.SNSEventHandler",
        "com.github.dnvriend.lambda.S3EventHandler"
      ).contains(superClassName)
  }

  def interfacesContainsRequestStreamHandlerInterface(interfaces: List[Class[_]]): Boolean = {
    val reqStreamHandler: Class[_] = classOf[RequestStreamHandler]
    interfaces.map(_.getName).contains(reqStreamHandler.getName)
  }

  def debugProjectClass(pc: ProjectClass): ProjectClass = {
    println(Show[ProjectClass].shows(pc))
    pc
  }

  /**
   * Only 'com.amazonaws.services.lambda.runtime.RequestStreamHandler', are lambdas in sbt-sam
   */
  def run(projectClasses: Set[ProjectClass]): Set[ProjectLambda] = {
    projectClasses
      //      .map(debugProjectClass)
      .filter(lambdaPredicate)
      .map(pc => ProjectLambda(pc))
  }
}

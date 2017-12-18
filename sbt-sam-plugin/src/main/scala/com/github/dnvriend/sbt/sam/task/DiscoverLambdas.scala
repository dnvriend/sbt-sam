package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.lambda.runtime.RequestStreamHandler

import scalaz.Show

case class HandlerName(value: String)
final case class ProjectLambda(projectClass: ProjectClass)

object DiscoverLambdas {
  def lambdaPredicate(pc: ProjectClass): Boolean = {
    interfacesContainsRequestStreamHandlerInterface(pc.interfaces)
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

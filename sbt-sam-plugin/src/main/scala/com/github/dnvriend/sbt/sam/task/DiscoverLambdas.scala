package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.lambda.runtime.RequestStreamHandler

final case class ProjectLambda(projectClass: ProjectClass)

object DiscoverLambdas {
  /**
    * Only 'com.amazonaws.services.lambda.runtime.RequestStreamHandler', are lambdas in sbt-sam
    */
  def run(projectClasses: Set[ProjectClass]): Set[ProjectLambda] = {
    val reqStreamHandler: Class[_] = classOf[RequestStreamHandler]
    projectClasses.filter(projectClass => {
      projectClass.cl.getInterfaces.toList.map(_.getName).contains(reqStreamHandler.getName)
    }).map(pc => ProjectLambda(pc))
  }
}

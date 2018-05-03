package com.github.dnvriend.ecs

import com.amazonaws.services.ecs.model._
import com.amazonaws.services.ecs.{AmazonECS, AmazonECSClientBuilder}

import scala.collection.JavaConverters._

object ECS {
  def client(): AmazonECS = {
    AmazonECSClientBuilder.defaultClient()
  }

  /**
    * Starts a new task using the specified task definition.
    */
  def runTask(client: AmazonECS,
              cluster: String,
              taskDefinition: String,
              count: Int,
              containerOverrides: List[ContainerOverride] = List.empty,
              subnets: List[String] = List.empty,
              securityGroups: List[String] = List.empty,
              assignPublicIp: Boolean = false,
              launchType: LaunchType = LaunchType.FARGATE,
              ): RunTaskResult = {
    client.runTask(
      new RunTaskRequest()
        .withCluster(cluster)
        .withCount(count)
        .withLaunchType(launchType)
        .withPlatformVersion("LATEST")
        .withTaskDefinition(taskDefinition)
        .withOverrides(
          new TaskOverride().withContainerOverrides(containerOverrides.asJava)
        )
        .withNetworkConfiguration(
          new NetworkConfiguration()
            .withAwsvpcConfiguration(
              new AwsVpcConfiguration()
                .withSubnets(subnets.asJava)
                .withAssignPublicIp(if(assignPublicIp) "ENABLED" else "DISABLED")
                .withSecurityGroups(securityGroups.asJava)
            )
        )
    )
  }

  /**
    * Stops a running task.
    */
  def stopTask(client: AmazonECS,
          cluster: String,
          task: String,
          reason: String = "",
         ): StopTaskResult = {
    client.stopTask(
      new StopTaskRequest()
        .withCluster(cluster)
        .withTask(task)
        .withReason(reason)
    )
  }

  /**
    * Returns a list of tasks for a specified cluster.
    */
  def listTasks(client: AmazonECS, cluster: String): ListTasksResult = {
    client.listTasks(new ListTasksRequest().withCluster(cluster))
  }
}
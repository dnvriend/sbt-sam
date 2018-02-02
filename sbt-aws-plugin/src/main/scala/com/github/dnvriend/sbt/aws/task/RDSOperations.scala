package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.rds.model.{ DBInstance, DescribeDBInstancesRequest }
import com.amazonaws.services.rds.{ AmazonRDS, AmazonRDSClientBuilder }

import scala.collection.JavaConverters._
import scala.util.Try

object RDSOperations {
  def client(): AmazonRDS = {
    AmazonRDSClientBuilder.defaultClient()
  }

  def getRDSInstance(instanceIdentifier: String): Option[DBInstance] = {
    val req = new DescribeDBInstancesRequest().withDBInstanceIdentifier(instanceIdentifier)
    Try(client().describeDBInstances(req).getDBInstances.asScala.toList.head).toOption
  }
}

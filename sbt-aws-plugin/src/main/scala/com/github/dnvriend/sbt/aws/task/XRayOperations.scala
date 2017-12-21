package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.xray._

///**
//  * batch-get-traces                         | get-service-graph
//get-trace-graph                          | get-trace-summaries
//put-telemetry-records                    | put-trace-segments
//help
//  */

/**
 * AWS  X-Ray  provides APIs for managing debug traces and retrieving service maps and other data
 * created by processing those traces.
 */
object XRayOperations {
  def client(): AWSXRay = {
    AWSXRayClientBuilder.defaultClient()
  }
}

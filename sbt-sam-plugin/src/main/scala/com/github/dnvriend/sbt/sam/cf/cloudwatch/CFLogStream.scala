package com.github.dnvriend.sbt.sam.cf.cloudwatch

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import play.api.libs.json.{Json, Writes}

object CFLogStream {
  implicit val writes: Writes[CFLogStream] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::Logs::LogStream",
        "Properties" -> Json.obj(

        )
      )
    )
  })
}

/**
  * The AWS::Logs::LogStream resource creates an Amazon CloudWatch Logs log stream in a log group.
  * A log stream represents the sequence of events coming from an application instance or resource that you are monitoring.
  */
case class CFLogStream(
                      logicalName: String,

                      /**
                        * The name of the log group where the log stream is created.
                        */
                      logGroupName: String,

                      /**
                        * The name of the log stream to create. The name must be unique within the log group.
                        */
                      logStreamName: String,
                      ) extends Resource
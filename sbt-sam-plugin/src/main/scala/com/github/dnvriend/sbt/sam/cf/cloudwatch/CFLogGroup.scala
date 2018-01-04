package com.github.dnvriend.sbt.sam.cf.cloudwatch

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import play.api.libs.json.{Json, Writes}

object CFLogGroup {
  implicit val writes: Writes[CFLogGroup] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::Logs::LogGroup",
        "Properties" -> Json.obj(
          "LogGroupName" -> logGroupName,
          "RetentionInDays" -> retensionInDays
        )
      )
    )
  })
}

/**
  * The AWS::Logs::LogGroup resource creates an Amazon CloudWatch Logs log group that defines common properties for
  * log streams, such as their retention and access control rules. Each log stream must belong to one log group.
  */
case class CFLogGroup(
                     logicalName: String,

                     /**
                       * The name of the log group.
                       */
                     logGroupName: String,

                     /**
                       * The number of days log events are kept in CloudWatch Logs. When a log event expires,
                       * CloudWatch Logs automatically deletes it.
                       *
                       * The number of days to retain the log events in the specified log group
                       *
                       * Possible values:  1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, and 3653.
                       */
                     retensionInDays: Int,
                     ) extends Resource {
  require(List(1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, 3653).contains(retensionInDays), "retensionInDays shouldBe one of 1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, and 3653")
}
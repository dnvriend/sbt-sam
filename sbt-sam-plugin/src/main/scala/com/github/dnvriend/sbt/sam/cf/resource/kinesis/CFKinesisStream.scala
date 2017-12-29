package com.github.dnvriend.sbt.sam.cf.resource.kinesis

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import play.api.libs.json.{Json, Writes}

object CFKinesisStream {
  implicit val writes: Writes[CFKinesisStream] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::Kinesis::Stream",
        "Properties" -> Json.obj(
          "Name" -> streamName,
          "ShardCount" -> shardCount,
          "RetentionPeriodHours" -> retensionPeriodHours,
          "Tags" -> Json.arr(
            Json.obj("Key" -> "sbt:sam:projectName", "Value" -> projectName),
            Json.obj("Key" -> "sbt:sam:projectVersion", "Value" -> projectVersion),
            Json.obj("Key" -> "sbt:sam:stage", "Value" -> samStage),
          ),
        )
      )
    )
  })

  def fromConfig(
                         logicalName: String,
                         streamName: String,
                         retensionPeriodHours: Int,
                         shardCount: Int,
                         projectName: String,
                         projectVersion: String,
                         samStage: String,
                       ): CFKinesisStream = {
    CFKinesisStream(
      logicalName,
      streamName,
      shardCount,
      retensionPeriodHours,
      projectName,
      projectVersion,
      samStage
    )
  }
}

case class CFKinesisStream(
                          logicalName: String,
                          streamName: String,
                          shardCount: Int,
                          retensionPeriodHours: Int,
                          projectName: String,
                          projectVersion: String,
                          samStage: String,
                        ) extends Resource

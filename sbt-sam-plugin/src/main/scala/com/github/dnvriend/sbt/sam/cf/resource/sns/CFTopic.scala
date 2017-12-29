package com.github.dnvriend.sbt.sam.cf.resource.sns

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import play.api.libs.json.{ Json, Writes }

object CFTopic {
  implicit val writes: Writes[CFTopic] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::SNS::Topic",
        "Properties" -> Json.obj(
          "DisplayName" -> displayName,
          "TopicName" -> topicName
        )
      )
    )
  })

  def fromConfig(
    logicalName: String,
    displayName: String,
    topicName: String): CFTopic = {
    CFTopic(logicalName, displayName, topicName)
  }
}
case class CFTopic(
    /**
     * Name of the resource; must be unique in the template
     */
    logicalName: String,

    /**
     * A developer-defined string that can be used to identify this SNS topic.
     */
    displayName: String,

    /**
     * A name for the topic
     */
    topicName: String) extends Resource

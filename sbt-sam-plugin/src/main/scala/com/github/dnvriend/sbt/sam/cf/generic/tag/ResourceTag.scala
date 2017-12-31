package com.github.dnvriend.sbt.sam.cf.generic.tag

import play.api.libs.json.{Json, Writes}

case object ResourceTag {
  implicit val writes: Writes[ResourceTag] = Writes.apply(model => {
    import model._
    Json.obj("Key" -> key, "Value" -> value)
  })

  def projectTags(projectName: String, projectVersion: String, samStage: String): List[ResourceTag] = {
    List(
      ResourceTag("sbt:sam:projectName", projectName),
      ResourceTag("sbt:sam:projectVersion", projectVersion),
      ResourceTag("sbt:sam:stage", samStage),
    )
  }
}

/**
  * You can use the AWS CloudFormation Resource Tags property to apply tags to resources, which can help you identify and
  * categorize those resources.
  */
case class ResourceTag(key: String, value: String)

package com.github.dnvriend.sbt.sam.cf.resource.codebuild

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{JsValue, Json, Writes}

import scalaz.syntax.all._
import scalaz.NonEmptyList

/**
  * The AWS::CodeBuild::Project resource configures how AWS CodeBuild builds your source code.
  * For example, it tells AWS CodeBuild where to get the source code and which build environment to use.
  */
object CFCBProject {
  implicit val writes: Writes[CFCBProject] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::CodeBuild::Project",
        "Properties" -> NonEmptyList(
          Json.obj()
        ).widen[JsValue].fold(JsMonoids.jsObjectMerge)
      )
    )
  })
}

case class CFCBProject(
                      logicalName: String,


                      ) extends Resource
package com.github.dnvriend.sbt.sam.cf.template

import com.github.dnvriend.sbt.sam.cf.template.output.ServerlessApiOutput
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{ Json, Writes }

import scalaz.std.AllInstances._
import scalaz.syntax.foldable._

object Output {
  implicit val writes: Writes[Output] = Writes.apply {
    case out: ServerlessApiOutput => ServerlessApiOutput.writes.writes(out)
  }
}

trait Output

object Outputs {
  implicit val writes: Writes[Outputs] = Writes.apply(model => {
    import model._
    Json.obj("Outputs" -> outputs.foldMap(Json.toJson(_))(JsMonoids.jsObjectMerge))
  })
}

case class Outputs(outputs: List[Output])

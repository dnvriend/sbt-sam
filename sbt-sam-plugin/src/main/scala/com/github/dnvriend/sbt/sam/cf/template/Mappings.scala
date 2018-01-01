package com.github.dnvriend.sbt.sam.cf.template

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{ Json, Writes }

import scalaz.syntax.foldable._
import scalaz.std.AllInstances._

object MappingNameValue {
  implicit val writes: Writes[MappingNameValue] = Writes.apply(model => {
    import model._
    Json.obj(name -> value)
  })
}
case class MappingNameValue(name: String, value: String)

object Mapping {
  implicit val writes: Writes[Mapping] = Writes.apply(model => {
    import model._
    Json.obj(name -> nameValues.foldMap(Json.toJson(_))(JsMonoids.jsObjectMerge))
  })
}
case class Mapping(name: String, nameValues: List[MappingNameValue])

object Mappings {
  implicit val writes: Writes[Mappings] = Writes.apply(model => {
    import model._
    Json.obj("Mappings" -> mappings.foldMap(Json.toJson(_))(JsMonoids.jsObjectMerge))
  })
}
case class Mappings(mappings: List[Mapping])


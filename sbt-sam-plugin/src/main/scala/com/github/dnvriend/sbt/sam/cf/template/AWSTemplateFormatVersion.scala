package com.github.dnvriend.sbt.sam.cf.template

import play.api.libs.json.{ Json, Writes }

object AWSTemplateFormatVersion {
  implicit val writes: Writes[AWSTemplateFormatVersion] = {
    Writes.apply(model => Json.obj("AWSTemplateFormatVersion" -> model.value))
  }
}

/**
 * The AWSTemplateFormatVersion section (optional) identifies the capabilities of the template.
 * The latest template format version is 2010-09-09 and is currently the only valid value.
 */
case class AWSTemplateFormatVersion(value: String = "2010-09-09")

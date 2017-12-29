package com.github.dnvriend.sbt.sam.cf.template

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.sam.cf.resource.s3.S3Bucket
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{ Json, Writes }

import scalaz._
import scalaz.Scalaz._

object Resources {
  implicit val writes: Writes[Resources] = Writes.apply(model => {
    import model._
    Json.obj("Resources" -> resources.foldMap(Json.toJson(_))(JsMonoids.jsObjectMerge))
  })

  def fromResources(s3Bucket: S3Bucket, resources: List[Resource] = List.empty): Resources = {
    Resources(NonEmptyList(s3Bucket, resources: _*))
  }
}

/**
 * The required Resources section declares the AWS resources that you want to include in the stack, such as
 * an Amazon EC2 instance or an Amazon S3 bucket.
 */
case class Resources(resources: NonEmptyList[Resource])

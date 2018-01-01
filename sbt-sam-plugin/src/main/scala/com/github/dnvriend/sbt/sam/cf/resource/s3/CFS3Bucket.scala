package com.github.dnvriend.sbt.sam.cf.resource.s3

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.generic.tag.ResourceTag
import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{JsObject, JsValue, Json, Writes}

import scalaz._
import scalaz.Scalaz._

object CorsRule {
  implicit val writes: Writes[CorsRule] = Writes.apply(model => {
    import model._
    Json.obj(
      "MaxAge" -> maxAge,
      "AllowedHeaders" -> allowedHeaders,
      "AllowedMethods" -> allowedMethods,
      "AllowedOrigins" -> allowedOrigins,
      "ExposedHeaders" -> exposedHeaders,
    )
  })

  final val AllowAllForWebsiteBucketCorsRules: List[CorsRule] = List(
    CorsRule(
      3000,
      List("*"),
      List("GET", "POST", "PUT", "HEAD"),
      List("*"),
      List.empty[String],
    )
  )
}

/**
  * Rules that define cross-origin resource sharing of objects in this bucket.
  *
  */
case class CorsRule(
                     /**
                       * The time in seconds that your browser is to cache the preflight response for the specified resource.
                       */
                     maxAge: Int,

                     /**
                       * Headers that are specified in the Access-Control-Request-Headers header. These headers are allowed
                       * in a preflight OPTIONS request. In response to any preflight OPTIONS request, Amazon S3 returns
                       * any requested headers that are allowed.
                       */
                     allowedHeaders: List[String],

                     /**
                       * An HTTP method that you allow the origin to execute. The valid values are GET, PUT, HEAD, POST, and DELETE.
                       */
                     allowedMethods: List[String],

                     /**
                       * An origin that you allow to send cross-domain requests.
                       */
                     allowedOrigins: List[String],

                     /**
                       * One or more headers in the response that are accessible to client applications
                       * (for example, from a JavaScript XMLHttpRequest object).
                       */
                     exposedHeaders: List[String],
                   )


object CorsRules {
  implicit val writes: Writes[CorsRules] = Writes.apply(model => {
    import model._
      Json.obj("CorsConfiguration" -> Json.obj(
        "CorsRules" -> Json.toJson(corsRules)
      )
    )
  })
}

case class CorsRules(corsRules: List[CorsRule])

/**
  * A canned access control list (ACL) that grants predefined permissions to the bucket.
  */
trait S3AccessControl
object S3AccessControl {

  def fromName(name: String): S3AccessControl = name.toLowerCase.trim match {
    case "private" => Private
    case "bucketownerfullcontrol" => BucketOwnerFullControl
  }

  /**
    * Owner gets FULL_CONTROL. The AuthenticatedUsers group gets READ access.
    */
  case object AuthenticatedRead extends S3AccessControl

  /**
    * Owner gets FULL_CONTROL. Amazon EC2 gets READ access to GET an Amazon Machine Image
    * (AMI) bundle from Amazon S3.
    */
  case object AwsExecRead extends S3AccessControl

  /**
    * Object owner gets FULL_CONTROL. Bucket owner gets READ access. If you specify this canned ACL when
    * creating a bucket, Amazon S3 ignores it.
    */
  case object BucketOwnerRead extends S3AccessControl

  /**
    * Both the object owner and the bucket owner get FULL_CONTROL over the object. If you specify this canned ACL
    * when creating a bucket, Amazon S3 ignores it.
    */
  case object BucketOwnerFullControl extends S3AccessControl

  /**
    * The LogDelivery group gets WRITE and READ_ACP permissions on the bucket.
    */
  case object LogDeliveryWrite extends S3AccessControl

  /**
    * Owner gets FULL_CONTROL. No one else has access rights (default).
    */
  case object Private extends S3AccessControl

  /**
    * Owner gets FULL_CONTROL. The AllUsers group gets READ access.
    */
  case object PublicRead extends S3AccessControl

  /**
    * Owner gets FULL_CONTROL. The AllUsers group gets READ and WRITE access.
    * Granting this on a bucket is generally not recommended.
    */
  case object PublicReadWrite extends S3AccessControl
}

/**
  * Enables multiple variants of all objects in this bucket. You might enable versioning to prevent objects
  * from being deleted or overwritten by mistake or to archive objects so that you can retrieve previous versions of them.
  */
trait VersioningConfigurationOption
object VersioningConfigurationOption {
  def fromBoolean(bool: Boolean): VersioningConfigurationOption = {
    if(bool) Enabled else Suspended
  }
  case object Enabled extends VersioningConfigurationOption
  case object Suspended extends VersioningConfigurationOption
}

object CFS3WebsiteConfiguration {
  implicit val writes: Writes[CFS3WebsiteConfiguration] = Writes.apply(model => {
    import model._
    Json.obj("WebsiteConfiguration" -> Json.obj(
      "ErrorDocument" -> errorDocument,
      "IndexDocument" -> indexDocument
    ))
  })
}

case class CFS3WebsiteConfiguration(indexDocument: String, errorDocument: String)

object CFS3Bucket {
  implicit val writes: Writes[CFS3Bucket] = Writes.apply(model => {
    import model._
    val properties: JsValue = NonEmptyList(
      Json.obj("AccessControl" -> accessControl.toString),
      Json.obj("BucketName" -> bucketName),
      Json.obj( "VersioningConfiguration" -> Json.obj("Status" -> versioningConfiguration.toString)),
      Json.toJson(corsRules),
      Json.toJson(websiteConfiguration),
    ).foldMap(identity)(JsMonoids.jsObjectMerge)
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::S3::Bucket",
        "Properties" -> properties
      )
    )
  })

  def deploymentBucket(bucketName: String, tags: List[ResourceTag]): CFS3Bucket = {
    CFS3Bucket(
      "SbtSamDeploymentBucket",
      S3AccessControl.BucketOwnerFullControl,
      bucketName,
      VersioningConfigurationOption.Enabled,
      tags,
      Option.empty,
      Option.empty
    )
  }
}

/**
  * The AWS::S3::Bucket resource creates an Amazon Simple Storage Service (Amazon S3) bucket in the same AWS Region
  * where you create the AWS CloudFormation stack.
  *
  * To control how AWS CloudFormation handles the bucket when the stack is deleted, you can set a deletion policy
  * for your bucket. For Amazon S3 buckets, you can choose to retain the bucket or to delete the bucket.
  */
final case class CFS3Bucket(
                           logicalName: String,
                           accessControl: S3AccessControl,
                           bucketName: String,
                           versioningConfiguration: VersioningConfigurationOption,
                           tags: List[ResourceTag] = List.empty,
                           websiteConfiguration: Option[CFS3WebsiteConfiguration] = None,
                           corsRules: Option[CorsRules] = None,
                           ) extends Resource {
  require(tags.lengthCompare(8) < 0, "No more than 7 tags are allowed")

  def ref: JsValue = CloudFormation.ref(logicalName)
}

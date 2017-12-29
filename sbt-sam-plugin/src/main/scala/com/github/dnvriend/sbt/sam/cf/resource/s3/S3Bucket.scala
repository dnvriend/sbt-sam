package com.github.dnvriend.sbt.sam.cf.resource.s3

import com.github.dnvriend.sbt.sam.cf.CloudFormation
import com.github.dnvriend.sbt.sam.cf.generic.tag.ResourceTag
import com.github.dnvriend.sbt.sam.cf.resource.Resource
import play.api.libs.json.{JsObject, JsValue, Json, Writes}

object CorsRule {
  val writes: Writes[CorsRule] = Writes.apply(model => {
    import model._
    Json.obj(
      "Id" -> id,
      "MaxAge" -> maxAge,
      "AllowedHeaders" -> allowedHeaders,
      "AllowedMethods" -> allowedMethods,
      "AllowedOrigins" -> allowedOrigins,
      "ExposedHeaders" -> exposedHeaders,
    )
  })
}

/**
  * Rules that define cross-origin resource sharing of objects in this bucket.
  *
  */
case class CorsRule(
                     id: String,
                     maxAge: Int,
                     allowedHeaders: List[String],
                     allowedMethods: List[String],
                     allowedOrigins: List[String],
                     exposedHeaders: List[String],
                   )

/**
  * A canned access control list (ACL) that grants predefined permissions to the bucket.
  */
trait S3AccessControl
object S3AccessControl {

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
  case object Enabled extends VersioningConfigurationOption
  case object Suspended extends VersioningConfigurationOption
}

object S3Bucket {
  implicit val writes: Writes[S3Bucket] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::S3::Bucket",
        "Properties" -> Json.obj(
          "AccessControl" -> accessControl.toString,
          "BucketName" -> bucketName,
          "VersioningConfiguration" -> Json.obj("Status" -> versioningConfiguration.toString)
        )
      )
    )
  })

  def deploymentBucket(bucketName: String, tags: List[ResourceTag]): S3Bucket = {
    S3Bucket(
      "SbtSamDeploymentBucket",
      S3AccessControl.BucketOwnerFullControl,
      bucketName,
      VersioningConfigurationOption.Enabled,
      tags
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
final case class S3Bucket(
                           logicalName: String,
                           accessControl: S3AccessControl,
                           bucketName: String,
                           versioningConfiguration: VersioningConfigurationOption,
                           tags: List[ResourceTag] = List.empty,
                           corsRules: List[CorsRule] = List.empty,
                         ) extends Resource {
  require(tags.lengthCompare(8) < 0, "No more than 7 tags are allowed")

  def ref: JsValue = CloudFormation.ref(logicalName)
}

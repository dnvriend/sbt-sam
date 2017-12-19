package com.github.dnvriend.sbt.aws.task

import com.amazonaws.event.{ ProgressEvent, ProgressEventType, SyncProgressListener }
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model._
import com.amazonaws.{ AmazonClientException, AmazonServiceException, AmazonWebServiceRequest }
import com.github.dnvriend.ops.Converter
import sbt._

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }
import scalaz.Disjunction

final case class BucketName(value: String)
final case class S3ObjectKey(value: String)
final case class S3Object(value: File)
object PutObjectSettings {
  implicit val request: Converter[PutObjectSettings, PutObjectRequest] = Converter.instance(settings => {
    new PutObjectRequest(settings.bucketName.value, settings.s3ObjectKey.value, settings.s3Object.value)
      .withCannedAcl(CannedAccessControlList.AuthenticatedRead)
  })
}
final case class PutObjectSettings(bucketName: BucketName, s3ObjectKey: S3ObjectKey, s3Object: S3Object)

final case class PutObjectResponse(response: Option[PutObjectResult], failure: Option[Throwable])

object DeleteObjectSettings {
  implicit val request: Converter[DeleteObjectSettings, DeleteObjectRequest] = Converter.instance(settings => {
    new DeleteObjectRequest(settings.s3BucketId.value, settings.s3ObjectKey.value)
  })
}
final case class DeleteObjectSettings(s3BucketId: BucketName, s3ObjectKey: S3ObjectKey)

final case class DeleteObjectResponse(failure: Option[Throwable])

object ListVersionsSettings {
  implicit val request: Converter[ListVersionsSettings, ListVersionsRequest] = Converter.instance(settings => {
    new ListVersionsRequest()
      .withBucketName(settings.bucketName.value)
      .withPrefix(settings.s3ObjectKey.value)
  })
}
final case class ListVersionsSettings(bucketName: BucketName, s3ObjectKey: S3ObjectKey)

final case class S3ObjectVersionId(value: String)

// inspired by https://github.com/quaich-project/quartercask/blob/master/util/src/main/scala/codes/bytes/quartercask/s3/AWSS3.scala
object S3Operations extends AwsProgressListenerOps {
  def client(): AmazonS3 = {
    AmazonS3ClientBuilder.defaultClient()
  }

  /**
   * Returns a list of all Amazon S3 buckets that the authenticated sender of the request owns. Users must
   * authenticate with a valid AWS Access Key ID that is registered with Amazon S3. Anonymous requests cannot list buckets,
   * and users cannot list buckets that they did not create.
   */
  def getBucket(bucketId: BucketName, client: AmazonS3): Option[Bucket] = {
    client.listBuckets().asScala.find(_.getName == bucketId.value)
  }

  /**
   * Creates a new Amazon S3 bucket with the specified name in the region that the client was created in. If no region or AWS S3 endpoint was specified when creating the client, the bucket will be created within the default (US) region, Region.US_Standard.
   */
  def createBucket(bucketId: BucketName, client: AmazonS3): Try[BucketName] = {
    try {
      client.createBucket(bucketId.value)
      Success(bucketId)
    } catch {
      case ex @ (_: AmazonClientException |
        _: AmazonServiceException) =>
        Failure(ex)
    }
  }

  /**
   * Uploads a new object to the specified Amazon S3 bucket. The PutObjectRequest contains all the details of the request,
   * including the bucket to upload to, the key the object will be uploaded under, and the file or input stream
   * containing the data to upload.
   */
  def putObject(
    settings: PutObjectSettings,
    client: AmazonS3)(implicit conv: Converter[PutObjectSettings, PutObjectRequest]): Disjunction[Throwable, PutObjectResult] = {
    Disjunction.fromTryCatchNonFatal {
      val objectRequest: PutObjectRequest = conv(settings)
      addProgressListener(objectRequest, settings.s3Object.value.length(), settings.s3Object.value.name)
      client.putObject(objectRequest)
    }
  }

  /**
   * Deletes the specified object in the specified bucket. Once deleted, the object can only be restored if versioning
   * was enabled when the object was deleted.
   */
  def deleteObject(
    settings: DeleteObjectSettings,
    client: AmazonS3)(implicit conv: Converter[DeleteObjectSettings, DeleteObjectRequest]): Disjunction[Throwable, Unit] = {
    Disjunction.fromTryCatchNonFatal(client.deleteObject(conv(settings)))
  }

  /**
   * Delete all versions of the specified object
   */
  def deleteAllVersioned(
    settings: DeleteObjectSettings,
    client: AmazonS3)(implicit
    listConv: Converter[ListVersionsSettings, ListVersionsRequest],
    deleteConv: Converter[DeleteObjectSettings, DeleteObjectRequest]): Unit = {
    listVersions(ListVersionsSettings(settings.s3BucketId, settings.s3ObjectKey), client).foreach { versions =>
      versions.map(_.getVersionId).foreach { version =>
        println(s"Deleting artifact version: '$version'")
        client.deleteVersion(new DeleteVersionRequest(
          settings.s3BucketId.value,
          settings.s3ObjectKey.value,
          version
        ))
      }
    }
  }

  /**
   * Returns a list of versions of the specified s3 object
   */
  def listVersions(
    settings: ListVersionsSettings,
    client: AmazonS3)(implicit conv: Converter[ListVersionsSettings, ListVersionsRequest]): Disjunction[Throwable, List[S3VersionSummary]] = {
    Disjunction.fromTryCatchNonFatal(client.listVersions(conv(settings)).getVersionSummaries.asScala.toList)
  }

  /**
   * Returns the latest version of an S3 object
   */
  def latestVersion(
    settings: ListVersionsSettings,
    client: AmazonS3)(implicit conv: Converter[ListVersionsSettings, ListVersionsRequest]): Option[S3ObjectVersionId] = for {
    summaries <- Try(client.listVersions(conv(settings)).getVersionSummaries.asScala).toOption
    latestVersion <- summaries.find(_.isLatest).map(_.getVersionId).map(S3ObjectVersionId.apply)
  } yield latestVersion

  private def addProgressListener(
    request: AmazonWebServiceRequest,
    fileSize: Long,
    key: String): Unit = {
    request.setGeneralProgressListener(new SyncProgressListener {
      var uploadedBytes = 0L
      val fileName = {
        val area = 30
        val n = new File(key).getName
        val l = n.length()
        if (l > area - 3)
          "..." + n.substring(l - area + 3)
        else
          n
      }
      override def progressChanged(progressEvent: ProgressEvent): Unit = {
        if (progressEvent.getEventType == ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT ||
          progressEvent.getEventType == ProgressEventType.RESPONSE_BYTE_TRANSFER_EVENT) {
          uploadedBytes = uploadedBytes + progressEvent.getBytesTransferred
        }
        print(progressBar(if (fileSize > 0) ((uploadedBytes * 100) / fileSize).toInt else 100))
        print(s"Lambda JAR -> S3")
        if (progressEvent.getEventType == ProgressEventType.TRANSFER_COMPLETED_EVENT)
          println()
      }
    })
  }

  /**
   * Progress bar code borrowed from https://github.com/sbt/sbt-s3/blob/master/src/main/scala/S3Plugin.scala
   */
  private def progressBar(percent: Int) = {
    val b = "=================================================="
    val s = "                                                  "
    val p = percent / 2
    val z: StringBuilder = new StringBuilder(80)
    z.append("\r[")
    z.append(b.substring(0, p))
    if (p < 50) { z.append("=>"); z.append(s.substring(p)) }
    z.append("]   ")
    if (p < 5) z.append(" ")
    if (p < 50) z.append(" ")
    z.append(percent)
    z.append("%   ")
    z.mkString
  }
}

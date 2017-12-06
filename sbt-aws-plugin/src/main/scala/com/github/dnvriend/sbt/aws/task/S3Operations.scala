package com.github.dnvriend.sbt.aws.task

import com.amazonaws.event.{ ProgressEvent, ProgressEventType, SyncProgressListener }
import com.amazonaws.{ AmazonClientException, AmazonServiceException, AmazonWebServiceRequest }
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model._
import com.github.dnvriend.ops.Converter
import sbt._

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }
import scalaz.Disjunction

final case class S3BucketId(value: String)
final case class S3ObjectKey(value: String)
final case class S3Object(value: File)
object PutObjectSettings {
  implicit val toPutObjectRequest: Converter[PutObjectSettings, PutObjectRequest] = Converter.instance(settings => {
    val objectRequest = new PutObjectRequest(settings.s3BucketId.value, settings.s3ObjectKey.value, settings.s3Object.value)
    objectRequest.setCannedAcl(CannedAccessControlList.AuthenticatedRead)
    objectRequest
  })
}
final case class PutObjectSettings(s3BucketId: S3BucketId, s3ObjectKey: S3ObjectKey, s3Object: S3Object)

object DeleteObjectSettings {
  implicit val toDeleteObjectRequest: Converter[DeleteObjectSettings, DeleteObjectRequest] = Converter.instance(settings => {
    new DeleteObjectRequest(settings.s3BucketId.value, settings.s3ObjectKey.value)
  })
}
final case class DeleteObjectSettings(s3BucketId: S3BucketId, s3ObjectKey: S3ObjectKey)

// inspired by https://github.com/quaich-project/quartercask/blob/master/util/src/main/scala/codes/bytes/quartercask/s3/AWSS3.scala
object S3Operations extends AwsProgressListenerOps {
  def client(cr: CredentialsAndRegion): AmazonS3 = {
    AmazonS3ClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }

  /**
   * Returns a list of all Amazon S3 buckets that the authenticated sender of the request owns. Users must
   * authenticate with a valid AWS Access Key ID that is registered with Amazon S3. Anonymous requests cannot list buckets,
   * and users cannot list buckets that they did not create.
   */
  def getBucket(bucketId: S3BucketId, client: AmazonS3): Option[Bucket] = {
    client.listBuckets().asScala.find(_.getName == bucketId.value)
  }

  /**
   * Creates a new Amazon S3 bucket with the specified name in the region that the client was created in. If no region or AWS S3 endpoint was specified when creating the client, the bucket will be created within the default (US) region, Region.US_Standard.
   */
  def createBucket(bucketId: S3BucketId, client: AmazonS3): Try[S3BucketId] = {
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
    println(conv(settings))
    Disjunction.fromTryCatchNonFatal(client.deleteObject(conv(settings).addPrintlnEventLogger))
  }

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

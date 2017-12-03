package com.github.dnvriend.sbt.aws.task

import com.amazonaws.event.{ ProgressEvent, ProgressEventType, SyncProgressListener }
import com.amazonaws.{ AmazonClientException, AmazonServiceException, AmazonWebServiceRequest }
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model.{ Bucket, CannedAccessControlList, PutObjectRequest }
import sbt._

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

final case class S3Key(value: String)
final case class S3KeyPrefix(value: String)
final case class S3BucketId(value: String)

// inspired by https://github.com/quaich-project/quartercask/blob/master/util/src/main/scala/codes/bytes/quartercask/s3/AWSS3.scala
object S3Operations {
  def client(cr: CredentialsAndRegion): AmazonS3 = {
    AmazonS3ClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }

  /**
   * Returns a list of all Amazon S3 buckets that the authenticated sender of the request owns. Users must authenticate with a valid AWS Access Key ID that is registered with Amazon S3. Anonymous requests cannot list buckets, and users cannot list buckets that they did not create.
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

  def pushJarToS3(
    jar: File,
    bucketId: S3BucketId,
    s3KeyPrefix: S3KeyPrefix,
    client: AmazonS3): Try[S3Key] = {
    try {
      val key: String = s3KeyPrefix.value + jar.getName
      val objectRequest = new PutObjectRequest(bucketId.value, key, jar)
      objectRequest.setCannedAcl(CannedAccessControlList.AuthenticatedRead)
      val objectMetadata = client.getObjectMetadata(bucketId.value, key)
      addProgressListener(objectRequest, objectMetadata.getContentLength, key)
      client.putObject(objectRequest)
      Success(S3Key(key))
    } catch {
      case ex @ (_: AmazonClientException |
        _: AmazonServiceException) =>
        Failure(ex)
    }
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

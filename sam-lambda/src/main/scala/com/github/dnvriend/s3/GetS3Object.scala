package com.github.dnvriend.s3

import java.io.InputStream

import com.amazonaws.services.s3.model.{ ObjectMetadata, S3Object }
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }

import scalaz._

object GetS3Object {
  def apply(bucketName: String, key: String): GetS3Object = {
    new GetS3Object(bucketName, key)
  }
}

/**
 * GetS3Object is a specialized service that gets data from an S3 Object. It
 * creates an S3 client whose connection will be used to stream data from S3 to
 * the process that needs the data. Streaming data from S3 needs special attention
 * in order to control resource usage therefore the GetS3Object provides some managed
 * methods in order to control the input stream when consuming data from S3.
 */
class GetS3Object(bucketName: String, key: String) {
  private val client: AmazonS3 = AmazonS3ClientBuilder.defaultClient()
  private val obj: Disjunction[Throwable, S3Object] = Disjunction.fromTryCatchNonFatal {
    client.getObject(bucketName, key)
  }

  def getObject: Option[S3Object] = obj.toOption
  /**
   * Gets the object stored in Amazon S3 under the specified bucket and key.
   */
  def getObjectD: Disjunction[Throwable, S3Object] = obj

  /**
   * Gets the metadata stored by Amazon S3 for this object. The ObjectMetadata object includes any custom
   * user metadata supplied by the caller when the object was uploaded, as well as HTTP metadata such as content
   * length and content type.
   */
  def getMetaData: Option[ObjectMetadata] = getObject.map(_.getObjectMetadata)
  /**
   * Gets the metadata stored by Amazon S3 for this object. The ObjectMetadata object includes any custom
   * user metadata supplied by the caller when the object was uploaded, as well as HTTP metadata such as content
   * length and content type.
   */
  def getMetaDataD: Disjunction[Throwable, ObjectMetadata] = getObjectD.map(_.getObjectMetadata)

  /**
   * Returns the S3ObjectInputStream as an java.io.InputStream
   * Be extremely careful when using this method; the returned Amazon S3 object contains a direct stream of data
   * from the HTTP connection. The underlying HTTP connection cannot be reused until the user finishes
   * reading the data and closes the stream. Also note that if not all data is read from the stream then
   * the SDK will abort the underlying connection, this may have a negative impact on performance.
   * Therefore:
   * - Use the data from the input stream in Amazon S3 object as soon as possible
   * - Read all data from the stream (use GetObjectRequest.setRange(long, long) to request only the bytes you need)
   * - Close the input stream in Amazon S3 object as soon as possible
   */
  def getInputStream: Option[InputStream] = getObject.map(_.getObjectContent)
  /**
   * Returns the S3ObjectInputStream as an java.io.InputStream
   * Be extremely careful when using this method; the returned Amazon S3 object contains a direct stream of data
   * from the HTTP connection. The underlying HTTP connection cannot be reused until the user finishes
   * reading the data and closes the stream. Also note that if not all data is read from the stream then
   * the SDK will abort the underlying connection, this may have a negative impact on performance.
   * Therefore:
   * - Use the data from the input stream in Amazon S3 object as soon as possible
   * - Read all data from the stream (use GetObjectRequest.setRange(long, long) to request only the bytes you need)
   * - Close the input stream in Amazon S3 object as soon as possible
   */
  def getInputStreamD: Disjunction[Throwable, InputStream] = getObjectD.map(_.getObjectContent)

  /**
   * Returns the S3ObjectInputStream as an java.io.InputStream
   * Be extremely careful when using this method; the returned Amazon S3 object contains a direct stream of data
   * from the HTTP connection. The underlying HTTP connection cannot be reused until the user finishes
   * reading the data and closes the stream. Also note that if not all data is read from the stream then
   * the SDK will abort the underlying connection, this may have a negative impact on performance.
   * Therefore:
   * - Use the data from the input stream in Amazon S3 object as soon as possible
   * - Read all data from the stream (use GetObjectRequest.setRange(long, long) to request only the bytes you need)
   * - Close the input stream in Amazon S3 object as soon as possible
   */
  def getInputStreamManaged[A](f: Option[InputStream] => A): A = getInputStream match {
    case right @ Some(is) => try f(right) finally is.close()
    case left             => f(left)
  }

  /**
   * Returns the S3ObjectInputStream as an java.io.InputStream
   * Be extremely careful when using this method; the returned Amazon S3 object contains a direct stream of data
   * from the HTTP connection. The underlying HTTP connection cannot be reused until the user finishes
   * reading the data and closes the stream. Also note that if not all data is read from the stream then
   * the SDK will abort the underlying connection, this may have a negative impact on performance.
   * Therefore:
   * - Use the data from the input stream in Amazon S3 object as soon as possible
   * - Read all data from the stream (use GetObjectRequest.setRange(long, long) to request only the bytes you need)
   * - Close the input stream in Amazon S3 object as soon as possible
   */
  def getInputStreamManagedD[A](f: Disjunction[Throwable, InputStream] => A): A = getInputStreamD match {
    case right @ DRight(is) => try f(right) finally is.close()
    case left               => f(left)
  }
}

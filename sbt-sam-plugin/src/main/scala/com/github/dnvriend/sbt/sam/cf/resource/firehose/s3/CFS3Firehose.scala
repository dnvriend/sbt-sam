package com.github.dnvriend.sbt.sam.cf.resource.firehose.s3

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{JsValue, Json, Writes}

import scalaz.Scalaz._
import scalaz._

object CFS3FirehoseDeliveryStreamProcessor {
  implicit val writes: Writes[CFS3FirehoseDeliveryStreamProcessor] = Writes.apply(model => {
    import model._
    Json.obj(
      "Type" -> "Lambda",
      "Parameters" -> Json.arr(
        Json.obj("ParameterName" -> "LambdaArn", "ParameterValue" -> lambdaArn),
        Json.obj("ParameterName" -> "NumberOfRetries", "ParameterValue" -> numberOfRetries),
        Json.obj("ParameterName" -> "RoleArn", "ParameterValue" -> roleArn),
        Json.obj("ParameterName" -> "BufferSizeInMBs", "ParameterValue" -> bufferSizeInMBs),
        Json.obj("ParameterName" -> "BufferIntervalInSeconds", "ParameterValue" -> bufferIntervalInSeconds)
      )
    )
  })
}

case class CFS3FirehoseDeliveryStreamProcessor(
                                                lambdaArn: String,
                                                numberOfRetries: Int,
                                                roleArn: String,
                                                bufferSizeInMBs: Int,
                                                bufferIntervalInSeconds: Int
                                              )

object CFS3FirehoseProcessingConfiguration {
  implicit val writes: Writes[CFS3FirehoseProcessingConfiguration] = Writes.apply(model => {
    import model._
    Json.obj(
      "ProcessingConfiguration" -> Json.obj(
        "Enabled" -> true,
        "Processors" -> Json.arr()
      )
    )
  })
}

case class CFS3FirehoseProcessingConfiguration(deliveryStreamProcessor: CFS3FirehoseDeliveryStreamProcessor)

object CFS3FirehoseBucketArn {
  implicit val writes: Writes[CFS3FirehoseBucketArn] = Writes.apply(model => {
    import model._
    Json.obj("BucketARN" -> bucketArn)
  })

  def fromConfig(bucketName: String): CFS3FirehoseBucketArn = {
    CFS3FirehoseBucketArn(s"arn:aws:s3:::$bucketName")
  }
}

case class CFS3FirehoseBucketArn(bucketArn: String)

object CFS3FirehoseDeliveryStreamType {
  implicit val writes: Writes[CFS3FirehoseDeliveryStreamType] = Writes.apply(model => {
    Json.obj("DeliveryStreamType" -> "KinesisStreamAsSource")
  })
}

case class CFS3FirehoseDeliveryStreamType(deliveryStreamType: String = "KinesisStreamAsSource")

object CFS3FirehoseKinesisStreamSourceConfiguration {
  implicit val writes: Writes[CFS3FirehoseKinesisStreamSourceConfiguration] = Writes.apply(model => {
    import model._
      Json.obj(
        "KinesisStreamARN" -> kinesisStreamArn,
        "RoleARN" -> roleArn
      )
  })

  def fromConfig(accountId: String, region: String, streamName: String, roleArn: String): CFS3FirehoseKinesisStreamSourceConfiguration = {
    val kinesisStreamArn = s"arn:aws:kinesis:$region:$accountId:stream/$streamName"
    CFS3FirehoseKinesisStreamSourceConfiguration(kinesisStreamArn, roleArn)
  }
}

case class CFS3FirehoseKinesisStreamSourceConfiguration(
                                                         /**
                                                           * The Amazon Resource Name (ARN) of the source Kinesis stream.
                                                           */
                                                         kinesisStreamArn: String,
                                                         /**
                                                           * The Amazon Resource Name (ARN) of the role that provides
                                                           * access to the source Kinesis stream.
                                                           */
                                                         roleArn: String)

object CFS3FirehoseKinesisStreamBufferingHints {
  implicit val writes: Writes[CFS3FirehoseKinesisStreamBufferingHints] = Writes.apply(model => {
    import model._
    Json.obj("BufferingHints" -> Json.obj(
      "IntervalInSeconds" -> bufferingIntervalInSeconds,
      "SizeInMBs" -> bufferingSize
    ))
  })
}

case class CFS3FirehoseKinesisStreamBufferingHints(
                                                    /**
                                                      * The length of time, in seconds, that Kinesis Firehose buffers incoming data before
                                                      * delivering it to the destination.
                                                      *
                                                      * Minimum value of 60. Maximum value of 900; default = 300 seconds
                                                      */
                                                    bufferingIntervalInSeconds: Int,

                                                    /**
                                                      * The size of the buffer, in MBs, that Kinesis Firehose uses for incoming data before
                                                      * delivering it to the destination.
                                                      *
                                                      * We recommend setting this parameter to a value greater than the amount of data you typically
                                                      * ingest into the delivery stream in 10 seconds. For example, if you typically ingest data at 1 MB/sec,
                                                      * the value should be 10 MB or higher.
                                                      *
                                                      * Minimum value of 1. Maximum value of 128; default = 5
                                                      */
                                                    bufferingSize: Int)

object CFS3FirehoseCompressionFormat {
  implicit val writes: Writes[CFS3FirehoseCompressionFormat] = Writes.apply(model => {
    import model._
    Json.obj("CompressionFormat" -> compression)
  })
}

case class CFS3FirehoseCompressionFormat(compression: String) {
  require(List("uncompressed", "gzip", "zip", "snappy").contains(compression.toLowerCase))
}

object CFS3FirehoseEncryptionConfiguration {
  implicit val writes: Writes[CFS3FirehoseEncryptionConfiguration] = Writes.apply(model => {
    import model._
    Json.obj(
      "EncryptionConfiguration" -> Json.obj(
        "KMSEncryptionConfig" -> Json.obj(
          "AWSKMSKeyARN" -> encryptionKeyArn
        )
      )
    )
  })
}

case class CFS3FirehoseEncryptionConfiguration(encryptionKeyArn: String)

/**
  * Must be its own script, using dependsOn to wait for the bucket, stream, lambda and role to be created
  * ie. a CFS3Firehose will generate dependent resources as a whole, ie. CFS3Firehose will be used
  * but will need dependent logicalNames. The S3Firehose is a primitive, but the things that glues them
  * together will be a 'kappa' resource, and if found, will generate the following script:
  *
  * - S3 bucket
  * - Kinesis Stream
  * - Optional Lambda
  * - Role
  * - Extended S3 Firehose
  */
object CFS3Firehose {
  implicit val writes: Writes[CFS3Firehose] = Writes.apply(model => {
    import model._
    val firehoseArn: String = kinesisStreamSourceConfiguration.roleArn
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::KinesisFirehose::DeliveryStream",
        "Properties" -> Json.obj(
          "DeliveryStreamName" -> deliveryStreamName,
          "DeliveryStreamType" -> "KinesisStreamAsSource",
          "KinesisStreamSourceConfiguration" -> Json.toJson(kinesisStreamSourceConfiguration),
          "ExtendedS3DestinationConfiguration" -> List(
            Json.obj("RoleARN" -> firehoseArn),
            Json.obj("Prefix" -> ""),
            Json.obj("S3BackupMode" -> "Disabled"),
            Json.toJson(bucketArn),
            Json.toJson(compression),
            Json.toJson(encryptionConfiguration),
            Json.toJson(streamBufferingHints)
          ).foldMap(identity)(JsMonoids.jsObjectMerge)
        )
      )
    )
  })
}

case class CFS3Firehose(
                         logicalName: String,
                         /**
                           * A name for the delivery stream.
                           */
                         deliveryStreamName: String,

                         /**
                           * The Amazon Resource Name (ARN) of the Amazon S3 bucket.
                           */
                         bucketArn: CFS3FirehoseBucketArn,

                         /**
                           * When a Kinesis stream is used as the source for the delivery stream,
                           * a Kinesis Data Firehose DeliveryStream KinesisStreamSourceConfiguration
                           * containing the Kinesis stream ARN and the role ARN for the source stream.
                           */
                         kinesisStreamSourceConfiguration: CFS3FirehoseKinesisStreamSourceConfiguration,

                         /**
                           * The BufferingHints property type specifies how Amazon Kinesis Firehose (Kinesis Firehose)
                           * buffers incoming data before delivering it to the destination. The first buffer condition
                           * that is satisfied triggers Kinesis Firehose to deliver the data.
                           */
                         streamBufferingHints: CFS3FirehoseKinesisStreamBufferingHints,

                         /**
                           * The data processing configuration for the Kinesis Firehose delivery stream.
                           */
                         deliveryStreamProcessor: Option[CFS3FirehoseDeliveryStreamProcessor] = None,

                         /**
                           * The compression format for the Kinesis Firehose delivery stream.
                           */
                         compression: CFS3FirehoseCompressionFormat = CFS3FirehoseCompressionFormat("uncompressed"),

                         /**
                           * The Amazon Resource Name (ARN) of the AWS KMS encryption key that Amazon S3
                           * uses to encrypt data delivered by the Kinesis Firehose stream.
                           * The key must belong to the same region as the
                           */
                         encryptionConfiguration: Option[CFS3FirehoseEncryptionConfiguration] = None,

                       ) extends Resource

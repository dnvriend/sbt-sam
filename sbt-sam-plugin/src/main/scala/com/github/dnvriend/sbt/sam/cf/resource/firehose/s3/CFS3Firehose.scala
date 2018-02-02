package com.github.dnvriend.sbt.sam.cf.resource.firehose.s3

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import play.api.libs.json.{Json, Writes}

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
    Json.obj(
      "ProcessingConfiguration" -> Json.obj(
        "Enabled" -> true,
        "Processors" -> Json.arr()
      )
    )
  })
}

case class CFS3FirehoseProcessingConfiguration(deliveryStreamProcessor: CFS3FirehoseDeliveryStreamProcessor)

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

object CFS3Firehose {
  implicit val writes: Writes[CFS3Firehose] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::KinesisFirehose::DeliveryStream",
        "DependsOn" -> dependsOnLogicalNames,
        "Properties" -> Json.obj(
          "DeliveryStreamName" -> deliveryStreamName,
          "DeliveryStreamType" -> "KinesisStreamAsSource",
          "KinesisStreamSourceConfiguration" ->  Json.obj(
            "KinesisStreamARN" -> kinesisStreamArn,
            "RoleARN" -> roleArn
          ),
          "ExtendedS3DestinationConfiguration" -> Json.obj(
            "RoleARN" -> roleArn,
            "Prefix" -> "",
            "S3BackupMode" -> "Disabled",
            "BucketARN" -> bucketArn,
            "CompressionFormat" -> compression,
            "BufferingHints" -> Json.obj(
              "IntervalInSeconds" -> bufferingIntervalInSeconds,
              "SizeInMBs" -> bufferingSize
            )
          )
        )
      )
    )
  })
}

/**
  * Delivery streams per region 20,
  * 2,000 transactions/second
  * 5,000 records/second
  * 5 MB/second
  *
  * The three capacity limits scale proportionally. For example, if you increase the throughput limit to 10MB/second,
  * the other limits increase to 4,000 transactions/second and 10,000 records/second.
  */
case class CFS3Firehose(
                         logicalName: String,
                         /**
                           * A name for the delivery stream.
                           */
                         deliveryStreamName: String,

                         /**
                           * Depends on S3 bucket, Kinesis Stream, IAM Policy
                           */
                         dependsOnLogicalNames: List[String],

                         /**
                           * Arn of the Kinesis stream
                           */
                         kinesisStreamArn: String,

                         /**
                           * Arn of the role to assume in order to provide the service in
                           * the AWS account.
                           */
                         roleArn: String,

                         /**
                           * The Amazon Resource Name (ARN) of the Amazon S3 bucket.
                           */
                         bucketArn: String,

                         /**
                           * The length of time, in seconds, that Kinesis Firehose buffers incoming data before
                           * delivering it to the destination. Buffer incoming data for the specified period of time,
                           * in seconds, before delivering it to the destination. The default value is 300.
                           *
                           * Min=60, Max=900; default=300
                           */
                         bufferingIntervalInSeconds: Int,

                         /**
                           * The size of the buffer, in MBs, that Kinesis Firehose uses for incoming data before
                           * delivering it to the destination. Buffer incoming data to the specified size, in MBs, before
                           * delivering it to the destination. The default value is 5.
                           *
                           * We recommend setting this parameter to a value greater than the amount of data you typically
                           * ingest into the delivery stream in 10 seconds. For example, if you typically ingest data at 1 MB/sec,
                           * the value should be 10 MB or higher.
                           *
                           * Min=1, Max=128; default=5
                           */
                         bufferingSize: Int,

                         /**
                           * The compression format for the Kinesis Firehose delivery stream.
                           */
                         compression: String,

                       ) extends Resource {

  require(deliveryStreamName.length <= 128, s"DeliveryStreamName should be less than 128 characters, deliveryStreamName is: '$deliveryStreamName', length is '${deliveryStreamName.length}'")
  require(deliveryStreamName.length > 1, s"DeliveryStreamName should be larger than than 1character, deliveryStreamName is: '$deliveryStreamName', length is '${deliveryStreamName.length}'")
}

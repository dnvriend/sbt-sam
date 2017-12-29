package com.github.dnvriend.sbt.sam.resource.firehose.s3.model

case class S3Firehose(
                     name: String,
                     bucketName: String,
                     configName: String = "",
                     roleArn: String,
                     kinesisStreamSource: Option[String] = None,
                     compression: Option[String] = None,
                     encryptionKey: Option[String] = None,
                     bufferingIntervalInSeconds: Int = 300,
                     bufferingSize: Int = 5,
                     export: Boolean = false,
                     )

package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.s3.AmazonS3
import com.github.dnvriend.sbt.aws.task._
import sbt.File
import sbt.util.Logger

object ArtifactUpload {
  def run(
    config: ProjectConfiguration,
    artifact: File,
    client: AmazonS3,
    log: Logger
  ): PutObjectResponse = {
    S3Operations.putObject(
      PutObjectSettings(
        BucketName(config.samS3BucketName.value),
        S3ObjectKey(artifact.getName),
        S3Object(artifact)
      ),
      client
    ).bimap(t => PutObjectResponse(None, Option(t)), result => PutObjectResponse(Option(result), None)).merge
  }
}

package com.github.dnvriend.sbt.sam.task

import com.amazonaws.services.s3.AmazonS3
import com.github.dnvriend.sbt.aws.task._
import sbt.File
import sbt.util.Logger

object ArtifactDelete {
  def run(
    config: ProjectConfiguration,
    artifact: File,
    client: AmazonS3,
    log: Logger
  ): DeleteObjectResponse = {
    S3Operations.deleteObject(
      DeleteObjectSettings(
        S3BucketId(config.samS3BucketName.value),
        S3ObjectKey(artifact.getName)
      ),
      client
    ).bimap(t => DeleteObjectResponse(Option(t)), _ => DeleteObjectResponse(None)).merge
  }
}

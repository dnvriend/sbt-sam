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
  ): Unit = {
    S3Operations.deleteAllVersioned(
      DeleteObjectSettings(
        BucketName(config.samS3BucketName.value),
        S3ObjectKey(artifact.getName)
      ),
      client
    )
  }
}

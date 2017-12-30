package com.github.dnvriend.sbt.sam.resource.bucket.model

case class S3Website(indexDocument: String, errorDocument: String)

object S3Bucket {

  /**
    * Creates a scoped bucket name by projectName and stage
    */
  def scopedBucketName(projectName: String, stage: String, name: String): String = {
    s"$projectName-$stage-$name".toLowerCase
  }

  /**
    * Returns the arn for a bucket by bucket name
    */
  def arn(name: String): String = {
    s"arn:aws:s3:::$name"
  }
}

case class S3Bucket(
                    name: String,
                    accessControl: String = "Private",
                    configName: String = "",
                    website: Option[S3Website] = None,
                    versioningEnabled: Boolean = false,
                    corsEnabled: Boolean = false,
                    accelerateEnabled: Boolean = false,
                    export: Boolean = false,
)

package com.github.dnvriend.sbt.sam.resource.bucket.model

case class S3Website(indexDocument: String, errorDocument: String)

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

package com.github.dnvriend.sbt.sam.resource

import com.github.dnvriend.sbt.sam.resource.bucket.model.{ S3Bucket, S3Website }
import com.github.dnvriend.test.TestSpec

class S3BucketResourceOperationsTest extends TestSpec {
  "s3 buckets config" should "read an empty configuration" in {
    ResourceOperations.retrieveTopics("".tsc) shouldBe Set()
  }

  it should "read a bucket without website configuration" in {
    ResourceOperations
      .retrieveBuckets(
        """
          |buckets {
          |   ImagesBucket {
          |    name = "images-bucket"
          |    access-control = "Private" // AuthenticatedRead | AwsExecRead | BucketOwnerRead | BucketOwnerFullControl | LogDeliveryWrite | Private | PublicRead | PublicReadWrite
          |    versioning-enabled = true
          |    cors-enabled = true
          |    accelerate-enabled = true
          |    export = true
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
      S3Bucket(
        "images-bucket",
        "Private",
        "ImagesBucket",
        None,
        versioningEnabled = true,
        corsEnabled = true,
        accelerateEnabled = true,
        export = true
      )
    )
  }

  it should "read a bucket with website configuration" in {
    ResourceOperations
      .retrieveBuckets(
        """
          |buckets {
          |   ImagesBucket {
          |    name = "images-bucket"
          |    access-control = "Private" // AuthenticatedRead | AwsExecRead | BucketOwnerRead | BucketOwnerFullControl | LogDeliveryWrite | Private | PublicRead | PublicReadWrite
          |    versioning-enabled = true
          |    cors-enabled = true
          |    accelerate-enabled = true
          |    export = true
          |
          |    website = {
          |       error-document = "error.html"
          |       index-document = "index.html"
          |    }
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          S3Bucket(
            "images-bucket",
            "Private",
            "ImagesBucket",
            Some(S3Website("index.html", "error.html")),
            versioningEnabled = true,
            corsEnabled = true,
            accelerateEnabled = true,
            export = true
          )
        )
  }

  it should "read multiple buckets" in {
    ResourceOperations
      .retrieveBuckets(
        """
          |buckets {
          |   ImagesBucket {
          |    name = "images-bucket"
          |    access-control = "Private" // AuthenticatedRead | AwsExecRead | BucketOwnerRead | BucketOwnerFullControl | LogDeliveryWrite | Private | PublicRead | PublicReadWrite
          |    versioning-enabled = true
          |    cors-enabled = true
          |    accelerate-enabled = true
          |    export = true
          |
          |    website = {
          |       error-document = "error.html"
          |       index-document = "index.html"
          |    }
          |  }
          |  ImagesBucket2 {
          |    name = "images-bucket2"
          |    access-control = "Private" // AuthenticatedRead | AwsExecRead | BucketOwnerRead | BucketOwnerFullControl | LogDeliveryWrite | Private | PublicRead | PublicReadWrite
          |    versioning-enabled = true
          |    cors-enabled = true
          |    accelerate-enabled = true
          |    export = true
          |
          |    website = {
          |       error-document = "error.html"
          |       index-document = "index.html"
          |    }
          |  }
          |   ImagesBucket3 {
          |    name = "images-bucket3"
          |    access-control = "Private" // AuthenticatedRead | AwsExecRead | BucketOwnerRead | BucketOwnerFullControl | LogDeliveryWrite | Private | PublicRead | PublicReadWrite
          |    versioning-enabled = true
          |    cors-enabled = true
          |    accelerate-enabled = true
          |    export = true
          |
          |    website = {
          |       error-document = "error.html"
          |       index-document = "index.html"
          |    }
          |  }
          |}
        """.stripMargin.tsc) should contain allOf(
      S3Bucket(
        "images-bucket",
        "Private",
        "ImagesBucket",
        Some(S3Website("index.html", "error.html")),
        versioningEnabled = true,
        corsEnabled = true,
        accelerateEnabled = true,
        export = true
      ),
      S3Bucket(
        "images-bucket2",
        "Private",
        "ImagesBucket2",
        Some(S3Website("index.html", "error.html")),
        versioningEnabled = true,
        corsEnabled = true,
        accelerateEnabled = true,
        export = true
      ),
      S3Bucket(
        "images-bucket3",
        "Private",
        "ImagesBucket3",
        Some(S3Website("index.html", "error.html")),
        versioningEnabled = true,
        corsEnabled = true,
        accelerateEnabled = true,
        export = true
      ),
    )
  }
}

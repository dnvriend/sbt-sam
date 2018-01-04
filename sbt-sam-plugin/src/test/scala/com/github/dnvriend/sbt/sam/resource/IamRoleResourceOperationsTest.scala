package com.github.dnvriend.sbt.sam.resource

import com.github.dnvriend.sbt.sam.resource.role.model.{ IamPolicyAllow, IamRole }
import com.github.dnvriend.test.TestSpec

class IamRoleResourceOperationsTest extends TestSpec {
  "roles config" should "read an empty configuration" in {
    ResourceOperations.retrieveRoles("".tsc) shouldBe Set()
  }

  it should "read a role" in {
    ResourceOperations
      .retrieveRoles(
        """
          |roles {
          |   ButtonClickedFirehoseRole {
          |    name = "firehose-access-role"
          |    allow-assume-role-principal = "firehose.amazonaws.com"
          |    managed-policy-arns = [
          |      "arn:aws:iam::aws:policy/AWSLambdaFullAccess",
          |      "arn:aws:iam::aws:policy/AmazonS3FullAccess",
          |      "arn:aws:iam::aws:policy/AmazonKinesisFullAccess",
          |      "arn:aws:iam::aws:policy/AmazonKinesisFirehoseFullAccess"
          |    ]
          |    allow = [
          |     {
          |       name = "firehose-access-role-s3-access-policy"
          |       actions = [
          |            "s3:AbortMultipartUpload",
          |            "s3:GetBucketLocation",
          |            "s3:GetObject",
          |            "s3:ListBucket",
          |            "s3:ListBucketMultipartUploads",
          |            "s3:PutObject"
          |       ]
          |       resources = [
          |         "arn:aws:s3:::firehose-bucket-name",
          |         "arn:aws:s3:::firehose-bucket-name/*"
          |       ]
          |     },
          |     {
          |       name = "firehose-access-role-kinesis-access-policy"
          |       actions = [
          |            "kinesis:DescribeStream",
          |            "kinesis:GetShardIterator",
          |            "kinesis:GetRecords"
          |       ]
          |       resources = [
          |         "arn:aws:kinesis:eu-west-1:123456789:stream/sam-seed-test-button-clicked"
          |       ]
          |     }
          |    ]
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          IamRole(
            "firehose-access-role",
            "ButtonClickedFirehoseRole",
            "firehose.amazonaws.com",
            List(
              "arn:aws:iam::aws:policy/AWSLambdaFullAccess",
              "arn:aws:iam::aws:policy/AmazonS3FullAccess",
              "arn:aws:iam::aws:policy/AmazonKinesisFullAccess",
              "arn:aws:iam::aws:policy/AmazonKinesisFirehoseFullAccess"),
            List(
              IamPolicyAllow(
                "firehose-access-role-s3-access-policy",
                List(
                  "s3:AbortMultipartUpload",
                  "s3:GetBucketLocation",
                  "s3:GetObject",
                  "s3:ListBucket",
                  "s3:ListBucketMultipartUploads",
                  "s3:PutObject"
                ),
                List(
                  "arn:aws:s3:::firehose-bucket-name",
                  "arn:aws:s3:::firehose-bucket-name/*"
                )
              ),
              IamPolicyAllow(
                "firehose-access-role-kinesis-access-policy",
                List(
                  "kinesis:DescribeStream",
                  "kinesis:GetShardIterator",
                  "kinesis:GetRecords"
                ),
                List(
                  "arn:aws:kinesis:eu-west-1:123456789:stream/sam-seed-test-button-clicked"
                )
              )
            )
          )
        )
  }
}

package com.github.dnvriend.sbt.sam.resource

import com.github.dnvriend.sbt.sam.resource.role.model.IamRole
import com.github.dnvriend.test.TestSpec

class RoleResourceOperationsTest extends TestSpec {
  "roles config" should "read an empty configuration" in {
    ResourceOperations.retrieveRoles("".tsc) shouldBe Set()
  }

  it should "read a role" in {
    ResourceOperations
      .retrieveRoles(
        """
          |roles {
          |   TestRole {
          |    name = "my-test-role"
          |    principal-service-name = "firehose.amazonaws.com"
          |    managed-policy-arns = [
          |      "arn:aws:iam::aws:policy/AWSLambdaFullAccess",
          |      "arn:aws:iam::aws:policy/AmazonS3FullAccess",
          |      "arn:aws:iam::aws:policy/AmazonKinesisFullAccess",
          |      "arn:aws:iam::aws:policy/AmazonKinesisFirehoseFullAccess"
          |    ]
          |    export = true
          |  }
          |}
        """.stripMargin.tsc) shouldBe Set(
          IamRole(
            "my-test-role",
            "TestRole",
            "firehose.amazonaws.com",
            List(
              "arn:aws:iam::aws:policy/AWSLambdaFullAccess",
              "arn:aws:iam::aws:policy/AmazonS3FullAccess",
              "arn:aws:iam::aws:policy/AmazonKinesisFullAccess",
              "arn:aws:iam::aws:policy/AmazonKinesisFirehoseFullAccess"
            ),
            true
          )
        )
  }
}

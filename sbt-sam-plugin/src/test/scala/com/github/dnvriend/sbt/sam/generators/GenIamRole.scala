package com.github.dnvriend.sbt.sam.generators

import com.github.dnvriend.sbt.sam.resource.role.model.{ IamPolicyAllow, IamRole }
import org.scalacheck.{ Arbitrary, Gen }

trait GenIamRole extends GenGeneric {
  val genIamPolicyAllow = for {
    name <- Gen.const("iam-policy-allow")
    actions <- Gen.containerOfN[List, String](5, Gen.oneOf(
      "s3:AbortMultipartUpload",
      "s3:GetBucketLocation",
      "s3:GetObject",
      "s3:ListBucket",
      "s3:ListBucketMultipartUploads",
      "s3:PutObject"
    ))
    resources <- Gen.containerOfN[List, String](5, Gen.oneOf(
      "arn:aws:kinesis:eu-west-1:123456789:stream/random-generated-resource",
      "arn:aws:s3:::random-generated-resource"
    ))
  } yield IamPolicyAllow(
    name,
    actions,
    resources
  )

  val genIamRole = for {
    configName <- genResourceConfName
    name <- Gen.const("iam-role")
    principalServiceName <- Gen.const("principal-service-name")
    managedPolicyArns <- Gen.containerOfN[List, String](3, Gen.oneOf(
      "arn:aws:iam::aws:policy/AWSLambdaFullAccess",
      "arn:aws:iam::aws:policy/AmazonS3FullAccess",
      "arn:aws:iam::aws:policy/AmazonKinesisFullAccess",
      "arn:aws:iam::aws:policy/AmazonKinesisFirehoseFullAccess"
    ))
    allow <- Gen.containerOfN[List, IamPolicyAllow](3, genIamPolicyAllow)
  } yield IamRole(
    name,
    configName,
    principalServiceName,
    managedPolicyArns,
    allow
  )

  implicit val arbIamRole: Arbitrary[IamRole] = Arbitrary.apply(genIamRole)

  val iterIamRole: Iterator[IamRole] = iterFor(genIamRole)
}
package com.github.dnvriend.sbt.aws.task

import com.amazonaws.regions.Regions
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.test.TestSpec

class IamOperations extends TestSpec with AllOps {
  val arnString = "arn:aws:iam::0123456789:user/dnvriend-git"
  it should "parse an arn" in {
    Arn.fromArnString(arnString.wrap[Arn]) shouldBe
      Arn(
        Partition("aws"),
        Service("iam"),
        Region(""),
        AccountId("0123456789"),
        ResourceType("user"),
        Resource("dnvriend-git"),
        arnString
      )
  }

  it should "parse a correct region" in {
    Region("eu-central-1").conv[Regions] shouldBe Regions.EU_CENTRAL_1
    Region("eu-west-1").conv[Regions] shouldBe Regions.EU_WEST_1
    Region("us-west-1").conv[Regions] shouldBe Regions.US_WEST_1
  }

  it should "parse in incorrect region" in {
    Region("").safeConv[Regions] shouldBe left[Throwable]
  }
}

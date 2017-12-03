package com.github.dnvriend.ops

import com.github.dnvriend.TestSpec

class ByteArrayOpsTest extends TestSpec with AllOps {
  it should "digest to md5" in {
    "person-repository-dev-sbtsamdeploymentbucket".arr.unwrap.md5.unwrap shouldBe "3D633624A7BF5439B4244A7CFC127A1D"
  }
}

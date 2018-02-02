package com.github.dnvriend.sbt.sam.resource

import com.github.dnvriend.sbt.sam.cf.resource.lambda.VPCConfig
import com.github.dnvriend.test.TestSpec

class VPCResourceOperationsTest extends TestSpec {
  "vpc config" should "read an empty configuration" in {
    ResourceOperations.retrieveVPCs("".tsc) shouldBe Set()
  }

  it should "read a vpc config" in {
    val vpcs = ResourceOperations.retrieveVPCs(
      """
        |vpcs {
        |   MyFirstVPC {
        |    subnet-ids = ["subnet-1", "subnet-2", "subnet-3"],
        |    security-group-ids = ["sg-1"]
        |  }
        |}
      """.stripMargin.tsc)

    vpcs.size shouldBe 1
    vpcs.head.id shouldEqual "MyFirstVPC"
    vpcs.head.securityGroupIds should contain allElementsOf List("sg-1")
    vpcs.head.subnetIds should contain allElementsOf List("subnet-1", "subnet-2", "subnet-3")
  }

  it should "read multiple vpc configs" in {
    val vpcs = ResourceOperations.retrieveVPCs(
      """
        |vpcs {
        |   MyFirstVPC {
        |    subnet-ids = ["subnet-1", "subnet-2", "subnet-3"],
        |    security-group-ids = ["sg-1"]
        |  }
        |  MySecondVPC {
        |    subnet-ids = ["subnet-4", "subnet-5", "subnet-6"],
        |    security-group-ids = ["sg-2"]
        |  }
        |}
      """.stripMargin.tsc
    )
    vpcs.size shouldBe 2

    vpcs.head.id shouldEqual "MyFirstVPC"
    vpcs.head.securityGroupIds should contain allElementsOf List("sg-1")
    vpcs.head.subnetIds should contain allElementsOf List("subnet-1", "subnet-2", "subnet-3")

    vpcs.drop(1).head.id shouldEqual "MySecondVPC"
    vpcs.drop(1).head.securityGroupIds should contain allElementsOf List("sg-2")
    vpcs.drop(1).head.subnetIds should contain allElementsOf List("subnet-4", "subnet-5", "subnet-6")
  }
}

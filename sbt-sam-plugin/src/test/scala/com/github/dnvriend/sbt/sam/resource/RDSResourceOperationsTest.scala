package com.github.dnvriend.sbt.sam.resource

import com.github.dnvriend.sbt.sam.cf.rds.RDSInstance
import com.github.dnvriend.test.TestSpec

class RDSResourceOperationsTest extends TestSpec {
  "rds config" should "read an empty configuration" in {
    ResourceOperations.retrieveRDSInstances("".tsc) shouldBe Set()
  }

  it should "read a RDS config" in {
    ResourceOperations.retrieveRDSInstances(
      """
        |rds {
        |  MyFirstRDS {
        |    db-instance-identifier = "foo"
        |    db-name = "dbname"
        |    allocated-storage = 5
        |    master-username = "admin"
        |    db-instance-class = "db.t2.micro"
        |    engine = "postgres"
        |    engine-version = "9.6.5"
        |    storage-type = "standard"
        |    master-password = "admin"
        |    multi-az = false,
        |    port = "5432"
        |    publicly-accessible = true,
        |    vpc-security-groups = ["sg-1"]
        |  }
        |}
      """.stripMargin.tsc) shouldBe Set(
        RDSInstance(
          "MyFirstRDS",
          "foo",
          "dbname",
          5,
          "db.t2.micro",
          "postgres",
          "9.6.5",
          None,
          None,
          "admin",
          "admin",
          false,
          "5432",
          true,
          "standard",
          None,
          List("sg-1")
        )
      )
  }

  it should "be able to extract multiple RDS instances from the following configuration" in {
    ResourceOperations.retrieveRDSInstances(
      """
        |rds {
        |  MyFirstRDS {
        |    db-instance-identifier = "foo"
        |    db-name = "dbname"
        |    allocated-storage = 5
        |    master-username = "admin"
        |    db-instance-class = "db.t2.micro"
        |    engine = "postgres"
        |    engine-version = "9.6.5"
        |    storage-type = "standard"
        |    master-password = "admin"
        |    multi-az = false,
        |    port = "5432"
        |    publicly-accessible = true,
        |    vpc-security-groups = ["sg-1"]
        |  }
        |  MySecondRDS {
        |   db-instance-identifier = "foo2"
        |   db-name = "dbname2"
        |   allocated-storage = 5
        |    master-username = "admin2"
        |   db-instance-class = "db.t2.micro"
        |   engine = "postgres"
        |   engine-version = "9.6.5"
        |   storage-type = "standard"
        |   master-password = "admin2"
        |   multi-az = true,
        |   port = "5432"
        |   publicly-accessible = false,
        |   vpc-security-groups = ["sg-1"]
        |  }
        |}
      """.stripMargin.tsc) should have size 2
  }

}

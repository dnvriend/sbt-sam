package com.github.dnvriend.sbt.sam.resource

import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.sbt.sam.resource.authorizer.{ CognitoAuthorizerType, Sigv4AuthorizerType }
import com.github.dnvriend.test.TestSpec

class AuthorizerOperationsTest extends TestSpec with AllOps {
  "authorizer type config" should "read an empty configuration" in {
    ResourceOperations.retrieveAuthorizerType("".tsc) shouldBe CognitoAuthorizerType
  }

  it should "read a sigv4 authorizer type" in {
    ResourceOperations.retrieveAuthorizerType("authorizer.type=sigv4".tsc) shouldBe Sigv4AuthorizerType
    ResourceOperations.retrieveAuthorizerType("""authorizer.type="sigv4" """.tsc) shouldBe Sigv4AuthorizerType
    ResourceOperations.retrieveAuthorizerType(
      """
        |authorizer {
        | type = "sigv4"
        |}
      """.stripMargin.tsc) shouldBe Sigv4AuthorizerType
  }

  it should "read a default cognito authorizer type" in {
    ResourceOperations.retrieveAuthorizerType("".tsc) shouldBe CognitoAuthorizerType
    ResourceOperations.retrieveAuthorizerType("authorizer.type=cognito".tsc) shouldBe CognitoAuthorizerType
    ResourceOperations.retrieveAuthorizerType("""authorizer.type="cognito" """.tsc) shouldBe CognitoAuthorizerType
    ResourceOperations.retrieveAuthorizerType(
      """
        |authorizer {
        |  type = "cognito"
        |}
      """.stripMargin.tsc) shouldBe CognitoAuthorizerType
  }
}

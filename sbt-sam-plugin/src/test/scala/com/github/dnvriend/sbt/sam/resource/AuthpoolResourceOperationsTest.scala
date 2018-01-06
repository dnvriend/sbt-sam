package com.github.dnvriend.sbt.sam.resource

import com.github.dnvriend.sbt.sam.resource.cognito.model.{Authpool, ImportAuthPool, PasswordPolicies, UserPoolUser}
import com.github.dnvriend.test.TestSpec

class AuthpoolResourceOperationsTest extends TestSpec {
  "authpool config" should "read an empty configuration" in {
    ResourceOperations.retrieveAuthPool("".tsc) shouldBe None
  }

  it should "read an authpool" in {
    ResourceOperations
      .retrieveAuthPool(
        """
          |cognito {
          |  AuthPool {
          |    name = "auth_pool"
          |    password-policies = {
          |        minimum-length = 6 # The minimum length of the password policy that you have set. Cannot be less than 6.
          |        require-lowercase = true # In the password policy that you have set, refers to whether you have required users to use at least one lowercase letter in their password.
          |        require-numbers = false  # In the password policy that you have set, refers to whether you have required users to use at least one number in their password.
          |        require-symbols = false # In the password policy that you have set, refers to whether you have required users to use at least one symbol in their password.
          |        require-uppercase = false # In the password policy that you have set, refers to whether you have required users to use at least one uppercase letter in their password.
          |    }
          |    users = [
          |     { username = "admin", password = "it-is-a-secret-2018" },
          |     { username = "dnvriend", password = "it-is-a-secret-2018" }
          |    ]
          |    export = true
          |  }
          |}
        """.stripMargin.tsc) shouldBe Option(
          Authpool(
            "auth_pool",
            PasswordPolicies(
              6,
              true,
              false,
              false,
              false
            ),
            List(
              UserPoolUser("admin", "it-is-a-secret-2018"),
              UserPoolUser("dnvriend", "it-is-a-secret-2018"),
            ),
            true
          )
        )
  }

  it should "read an import authpool" in {
    ResourceOperations
      .retrieveImportAuthPool(
        """
          |cognito {
          |  AuthPool {
          |    import-resource = "componentName:resourceName"
          |  }
          |}
        """.stripMargin.tsc, None) shouldBe Option(
      ImportAuthPool("componentName:resourceName")
    )
  }
}

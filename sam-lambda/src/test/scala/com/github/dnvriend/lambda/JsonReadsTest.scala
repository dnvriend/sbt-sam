package com.github.dnvriend.lambda

import com.github.dnvriend.test.TestSpec
import play.api.libs.json.Json

class JsonReadsTest extends TestSpec {
  it should "return none when reading nothing" in {
    Json.toJson("foo").asOpt[Nothing](JsonReads.nothingReads) shouldBe 'empty
  }
}

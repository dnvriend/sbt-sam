package com.github.dnvriend.ops

import com.github.dnvriend.test.TestSpec
import play.api.libs.json.{ JsValue, Json }

import scalaz.{ @@, Tag }

class JsonOpsTest extends TestSpec with JsonOps {
  val text: String = "HelloWorld"
  val taggedBytes: Array[Byte] @@ UTF8 = Tag(""""HelloWorld"""".getBytes("UTF-8"))
  val json: JsValue = Json.toJson(text)
  it should "escape json" in {
    json.escapedJson.toString shouldBe """"\"HelloWorld\"""""
  }

  it should "convert json to string" in {
    json.str shouldBe """"HelloWorld""""
  }

  it should "pretty print json" in {
    json.pretty shouldBe """"HelloWorld""""
  }

  it should "create utf tagged bytes" in {
    json.bytes shouldBe taggedBytes
  }

  it should "create yaml" in {
    val result = Json.obj("person" -> Json.obj("name" -> "dnvriend", "age" -> 42)).toYaml.trim
    result shouldBe
      """person:
        |  name: dnvriend
        |  age: 42""".stripMargin
  }
}

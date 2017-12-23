package com.github.dnvriend.ops

import java.io.InputStream
import java.nio.ByteBuffer

import com.github.dnvriend.test.TestSpec
import play.api.libs.json.{ JsValue, Json }

import scalaz.@@

class ByteArrayOpsTest extends TestSpec with ByteArrayOps {

  "byte array extensions" should "compress and decompress" in {
    forAll { (bytes: Array[Byte]) =>
      bytes.compress.decompress shouldBe bytes
    }
  }

  it should "convert to inputstream" in {
    forAll { (bytes: Array[Byte]) =>
      bytes.toInputStream shouldBe a[InputStream]
    }
  }

  it should "convert to byte buffer" in {
    forAll { (bytes: Array[Byte]) =>
      bytes.toByteBuffer shouldBe a[ByteBuffer]
    }
  }

  it should "convert to utf8 string" in {
    forAll { (str: String) =>
      str.getBytes("UTF-8").toUtf8String shouldBe str
      str.getBytes("UTF-8").str shouldBe str
    }
  }

  it should "encode to base64" in {
    forAll { (bytes: Array[Byte]) =>
      bytes.base64 shouldBe a[String @@ Base64]
    }
  }

  it should "encode to hex" in {
    forAll { (bytes: Array[Byte]) =>
      bytes.base64 shouldBe a[String @@ Hex]
    }
  }

  it should "encode to sha-256" in {
    forAll { (bytes: Array[Byte]) =>
      bytes.base64 shouldBe a[String @@ Hex]
    }
  }

  it should "encode to json" in {
    forAll { (name: String, age: String) =>
      val json: JsValue = Json.obj("name" -> name, "age" -> age)
      val jsBytes = json.toString.getBytes("UTF-8")
      jsBytes.json shouldBe json
    }
  }
}
package com.github.dnvriend.ops

import java.io.InputStream

import com.github.dnvriend.test.TestSpec

import scalaz.@@

class StringOpsTest extends TestSpec with StringOps {
  "string extensions" should "convert to input stream" in {
    "test".toInputStream shouldBe a[InputStream]
  }

  it should "convert to utf encoded byte array" in {
    "test".toUtf8Array shouldBe a[String @@ UTF8]
    "test".arr shouldBe a[String @@ UTF8]
  }

  it should "log a string to system out" in {
    "test".log
  }

  it should "find a text in a sentence" in {
    "this and then that".find("that".r).value shouldBe "that"
  }

  it should "find all text elements" in {
    "this and then that before this".findAll("this".r) shouldBe List("this", "this")
  }
}

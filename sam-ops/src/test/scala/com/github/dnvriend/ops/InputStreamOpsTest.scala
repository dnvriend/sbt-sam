package com.github.dnvriend.ops

import java.io.{ ByteArrayInputStream, InputStream }

import com.github.dnvriend.test.TestSpec

class InputStreamOpsTest extends TestSpec with InputStreamOps {
  it should "read an input stream and return a byte array" in {
    val text: String = "HelloWorld"
    val textBytes: Array[Byte] = text.getBytes("UTF-8")
    val textInputStream: InputStream = new ByteArrayInputStream(textBytes)
    textInputStream.toByteArray shouldBe textBytes
  }
}

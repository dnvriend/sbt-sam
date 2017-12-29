package com.github.dnvriend.ops

import java.nio.ByteBuffer

import com.github.dnvriend.test.TestSpec

class ByteBufferOpsTest extends TestSpec with ByteBufferOps {
  it should "convert a bytebuffer to an array" in {
    val buff = ByteBuffer.wrap(Array.empty[Byte])
    buff.toByteArray shouldBe Array.empty[Byte]
  }
}

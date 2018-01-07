package com.github.dnvriend.sam.serialization.serializer

import com.github.dnvriend.test.TestSpec

class DefaultInstanceTest extends TestSpec {
  it should "instantiate a default instance for a class" in {
    DefaultInstance.apply[DefaultInstanceTestDomainPerson] should beRight(DefaultInstanceTestDomainPerson())
  }
}

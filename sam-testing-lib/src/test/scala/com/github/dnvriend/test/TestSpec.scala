package com.github.dnvriend.test

import com.typesafe.config.{ Config, ConfigFactory }
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ FlatSpec, Matchers, OptionValues, TryValues }
import org.typelevel.scalatest.{ DisjunctionMatchers, DisjunctionValues, ValidationMatchers }

abstract class TestSpec extends FlatSpec
  with Matchers
  with ValidationMatchers
  with DisjunctionMatchers
  with DisjunctionValues
  with OptionValues
  with TryValues
  with PropertyChecks {

  implicit class TypesafeConfigOps(that: String) {
    def tsc: Config = {
      ConfigFactory.parseString(that)
    }
  }

  def interceptStdOut(f: => Unit): String = {
    val stream = new java.io.ByteArrayOutputStream()
    try Console.withOut(stream)(f) finally {
      stream.flush()
      stream.close()
    }
    new String(stream.toByteArray)
  }
}

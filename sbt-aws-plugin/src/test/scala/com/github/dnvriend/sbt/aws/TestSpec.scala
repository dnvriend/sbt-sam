package com.github.dnvriend.sbt.aws

import com.github.dnvriend.ops.AllOps
import org.scalatest.{ FlatSpec, Matchers, OptionValues, TryValues }
import org.typelevel.scalatest.{ DisjunctionMatchers, DisjunctionValues, ValidationMatchers }

abstract class TestSpec extends FlatSpec
  with Matchers
  with ValidationMatchers
  with DisjunctionMatchers
  with DisjunctionValues
  with OptionValues
  with TryValues
  with AllOps

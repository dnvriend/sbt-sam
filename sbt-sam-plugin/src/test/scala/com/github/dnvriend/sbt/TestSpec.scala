package com.github.dnvriend.sbt

import org.scalatest.{ FlatSpec, Matchers, OptionValues, TryValues }
import org.typelevel.scalatest.{ DisjunctionMatchers, DisjunctionValues, ValidationMatchers, ValidationValues }

abstract class TestSpec extends FlatSpec
  with Matchers
  with OptionValues
  with TryValues
  with DisjunctionMatchers
  with DisjunctionValues
  with ValidationMatchers
  with ValidationValues


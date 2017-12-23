package com.github.dnvriend.test

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
  with PropertyChecks

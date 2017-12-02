// Copyright 2017 Dennis Vriend
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.dnvriend.ops

import java.io.{ ByteArrayInputStream, InputStream }

import scala.util.matching.Regex
import scalaz.{ @@, Tag }

object StringOps extends StringOps {
  def apply(that: String): ToStringOps = new ToStringOps(that)
  def apply(that: String @@ Hex) = new ToHexStringOps(that)
}

trait StringOps {
  implicit def ToStringOps(that: String): ToStringOps = StringOps(that)
  implicit def ToHexStringOps(that: String @@ Hex): ToHexStringOps = new ToHexStringOps(that)
  implicit def ToBase64StringOps(that: String @@ Base64): ToBase64StringOps = new ToBase64StringOps(that)
}

class ToHexStringOps(that: String @@ Hex) {
  def fromHex: Array[Byte] = {
    javax.xml.bind.DatatypeConverter.parseHexBinary(Tag.unwrap(that))
  }
  def parseHex: Array[Byte] = {
    fromHex
  }
  def parse: Array[Byte] = {
    fromHex
  }
}

class ToBase64StringOps(that: String @@ Base64) {
  def fromBase64: Array[Byte] = {
    java.util.Base64.getDecoder.decode(Tag.unwrap(that))
  }
  def parseBase64: Array[Byte] = {
    fromBase64
  }
  def parse: Array[Byte] = {
    fromBase64
  }
}

class ToStringOps(that: String) {
  def toInputStream: InputStream = {
    new ByteArrayInputStream(that.getBytes)
  }
  def toUtf8Array: Array[Byte] @@ UTF8 = {
    Tag(that.getBytes("UTF-8"))
  }

  def arr: Array[Byte] @@ UTF8 = {
    toUtf8Array
  }

  def log: String = {
    println(that)
    that
  }

  def find(regex: Regex): Option[String] = {
    regex.findFirstIn(that)
  }

  def findAll(regex: Regex): List[String] = {
    regex.findAllIn(that).toList
  }

  def tagHex: String @@ Hex = Tag(that)
  def tagBase64: String @@ Base64 = Tag(that)
}
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

import java.io.OutputStream

import play.api.libs.json._

import scalaz._
import scalaz.Scalaz._

trait JsonOps {
  implicit def ToJsonOpsImpl(that: JsValue): JsonOpsImpl = new JsonOpsImpl(that)
  implicit def ToOutputStreamOps[A <: Product: Writes](that: A) = new JsonToOutputStreamOps(that)
}

class JsonOpsImpl(that: JsValue) extends StringOps {
  def resp: JsValue = {
    println(s"Response\n${Json.prettyPrint(that)}")
    that
  }

  def escapedJson: JsValue = {
    Json.toJson(that.toString)
  }

  def str: String = {
    that.toString()
  }

  def pretty: String = {
    Json.prettyPrint(that)
  }

  def bytes: Array[Byte] @@ UTF8 = {
    that.toString.arr
  }

  def validate[A: Reads]: Disjunction[Seq[(JsPath, Seq[JsonValidationError])], A] = {
    that.validate[A].asEither.disjunction
  }
}

class JsonToOutputStreamOps[A <: Product: Writes](that: A) extends StringOps with ByteArrayOps with AnyOps {
  def write(os: OutputStream): Unit = {
    val utf8EncodedArray: Array[Byte] = Json.toJson(that).toString().toUtf8Array.unwrap
    os.write(utf8EncodedArray)
    os.close()
  }
}
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

import scalaj.http.Http
import scalaz.{ Disjunction, Show }

object HttpOps extends HttpOps

trait HttpOps extends ByteArrayOps {
  implicit def ToHttpOps(that: String): ToHttpOpsImpl = new ToHttpOpsImpl(that)
  implicit def ToHttpStringContextOps(that: StringContext): ToHttpStringContextOps = new ToHttpStringContextOps(that)
  implicit def ToHttpsStringContextOps(that: StringContext): ToHttpsStringContextOps = new ToHttpsStringContextOps(that)
  implicit def ToHttpCommandOps(that: HttpCommand): ToHttpCommandOps = new ToHttpCommandOps(that)
  implicit def ToHttpsCommandOps(that: HttpsCommand): ToHttpsCommandOps = new ToHttpsCommandOps(that)

  implicit val HttpResponseShow = Show.shows[HttpResponse](resp => {
    s"""============================================
        |Http Response: ${resp.code}
        |============================================
        |Headers:
        |============================================
        |${resp.headers.map({ case (key, xs) => s"$key -> ${xs.mkString("\n")}" }).mkString("\n")}
        |============================================
        |Body length: ${resp.body.length}
        |============================================
        |${resp.body.str}
        |============================================
      """.stripMargin
  })
}

case class HttpResponse(body: Array[Byte], code: Int, headers: Map[String, IndexedSeq[String]])

class ToHttpOpsImpl(that: String) extends FunctionalOps {
  def get(headers: (String, String)*): Disjunction[Throwable, HttpResponse] = {
    val result = Http(that)
      .headers(headers)
      .timeout(Int.MaxValue, Int.MaxValue)
      .asBytes
    HttpResponse(result.body, result.code, result.headers)
  }.safe

  def post(data: Array[Byte], headers: (String, String)*): Disjunction[Throwable, HttpResponse] = {
    val result = Http(that)
      .headers(headers)
      .postData(data)
      .timeout(Int.MaxValue, Int.MaxValue)
      .asBytes
    HttpResponse(result.body, result.code, result.headers)
  }.safe

  def put(data: Array[Byte], headers: (String, String)*): Disjunction[Throwable, HttpResponse] = {
    val result = Http(that)
      .headers(headers)
      .put(data)
      .timeout(Int.MaxValue, Int.MaxValue)
      .asBytes
    HttpResponse(result.body, result.code, result.headers)
  }.safe
}

class ToHttpCommandOps(that: HttpCommand) extends ToHttpOpsImpl("http://" + that.url)
case class HttpCommand(url: String)
class ToHttpsCommandOps(that: HttpsCommand) extends ToHttpOpsImpl("https://" + that.url)
case class HttpsCommand(url: String)

/**
 * val name = "James"
 * s"Hello, $name"
 *
 * will be rewritten to:
 *
 * StringContext("Hello, ", "").s(name)
 */
class ToHttpStringContextOps(sc: StringContext) {
  def http(args: Any*): HttpCommand = {
    val str = sc.standardInterpolator(identity, args)
    HttpCommand(str.replace("http://", ""))
  }
}

class ToHttpsStringContextOps(sc: StringContext) {
  def https(args: Any*): HttpsCommand = {
    val str = sc.standardInterpolator(identity, args)
    HttpsCommand(str.replace("https://", ""))
  }
}
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

package com.github.dnvriend.lambda

import play.api.libs.json.{ Format, JsNull, JsValue, Json }

object HttpResponse {
  implicit val format: Format[HttpResponse] = Json.format[HttpResponse]

  val ok: HttpResponse = HttpResponse(200, JsNull, Map.empty[String, String])
  val validationError: HttpResponse = ok.copy(statusCode = 400)
  val notFound: HttpResponse = ok.copy(statusCode = 404)
  val serverError: HttpResponse = ok.copy(statusCode = 500)
}

case class HttpResponse(statusCode: Int, body: JsValue, headers: Map[String, String]) {
  def withBody(data: JsValue): HttpResponse = copy(body = data)

  def withHeader(name: String, value: String): HttpResponse = copy(headers = headers + (name -> value))

  def ok: HttpResponse = copy(statusCode = 200)

  def notFound: HttpResponse = copy(statusCode = 404)

  def validationError: HttpResponse = copy(statusCode = 400)

  def serverError: HttpResponse = copy(statusCode = 500)
}

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

import play.api.libs.json.{ Format, JsValue, Json }

object HttpResponse {
  implicit val format: Format[HttpResponse] = Json.format[HttpResponse]
}

case class HttpResponse(statusCode: Int, body: JsValue, headers: Map[String, String]) {
  def withBody(data: JsValue): HttpResponse = copy(body = data)

  def ok: HttpResponse = copy(statusCode = 200)

  def notFound: HttpResponse = copy(statusCode = 404)

  def serverError: HttpResponse = copy(statusCode = 500)
}

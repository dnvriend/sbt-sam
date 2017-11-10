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

import java.io.InputStream

import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.ops.DisjunctionNel.DisjunctionNel
import play.api.libs.json.{ JsValue, Json, Reads }

import scalaz._
import scalaz.Scalaz._

object HttpRequest {
  def parse(input: InputStream): HttpRequest = {
    HttpRequest(Json.parse(input))
  }
}

case class HttpRequest(request: JsValue) extends AllOps {
  def body: JsValue = request("body")

  def pathParameters: JsValue = request("pathParameters")

  def requestParameters: JsValue = request("queryStringParameters")

  def bodyOpt[A: Reads]: Option[A] = bodyAs[A].toOption

  def bodyAs[A: Reads](implicit validator: Validator[A] = null): DisjunctionNel[String, A] = {
    for {
      data <- Json.parse(body.toString).as[A].safe.leftMap(t => s"Could not deserialize request:\n$request".wrapNel)
      validated <- (validator.? | Validator.empty).validate(data).disjunction
    } yield validated
  }

  def bodyAsStringOpt: DisjunctionNel[String, String] = body.as[String].safe.leftMap(t => "Could not parse body to String".wrapNel)

  def pathParamsOpt[A: Reads]: Option[A] = pathParamAs.toOption

  def pathParamAs[A: Reads]: DisjunctionNel[String, A] = pathParameters.as[A].safe.leftMap(t => "Invalid request".wrapNel)

  def requestParamsOpt[A: Reads]: Option[A] = requestParamsAs.toOption

  def requestParamsAs[A: Reads]: Disjunction[Throwable, A] = requestParameters.as[A].safe
}

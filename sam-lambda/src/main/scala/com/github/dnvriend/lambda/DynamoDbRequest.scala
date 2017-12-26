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

import play.api.libs.json.{ JsValue, Json, Reads }

object DynamoDbRequest {
  def parse(input: InputStream): DynamoDbRequest = {
    DynamoDbRequest(Json.parse(input))
  }
  def parse(str: String): DynamoDbRequest = {
    DynamoDbRequest(Json.parse(str))
  }
}

case class DynamoDbRequest(json: JsValue) {
  def getInsertedKeys[K: Reads]: List[K] = {
    (json \ "Records").as[List[JsValue]]
      .filter(record => (record \ "eventName").as[String] == "INSERT")
      .map(record => (record \ "dynamodb" \ "Keys").as[K])
  }
}

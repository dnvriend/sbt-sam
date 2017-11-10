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

package com.github.dnvriend.sbt

import play.api.libs.json.Json

package object aws {
  final val AWS_COGNITO_DEFAULT_USER_PASSWORD = "awsplugindefaultpassword"
  final val DEFAULT_PROFILE = "default"
  final val DEFAULT_REGION = "eu-west-1"
  final val EMPTY = ""

  object Cognito {
    object UserPool {
      implicit val format = Json.format[UserPool]
    }
    case class UserPool(Id: String, Name: String)
    object User {
      implicit val format = Json.format[User]
    }
    case class User(Username: String, Enabled: Boolean, UserStatus: String, UserCreateDate: Double, UserLastModifiedDate: Double)

    object UserPoolClient {
      implicit val format = Json.format[UserPoolClient]
    }
    case class UserPoolClient(ClientName: String, UserPoolId: String, ClientId: String)

    case class AuthTokens(accessToken: String, expiresIn: Int, tokenType: String, refreshToken: String, idToken: String)
  }

  object Dynamo {
    case class Stream(arn: String, viewType: String, enabled: Boolean)
    case class Table(arn: String, name: String, status: String, itemCount: Long, sizeInBytes: Long, stream: Option[Stream])
  }
}

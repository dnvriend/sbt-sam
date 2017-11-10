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

package com.github.dnvriend.sbt.aws

import sbt.Logger

import scala.util.Try

object AwsAuthenticationService {

  import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder
  import com.amazonaws.services.cognitoidp.model.{ AdminInitiateAuthRequest, AuthFlowType, AuthenticationResultType }

  import scala.collection.JavaConverters._

  def run(profile: String, clientId: String, userPoolId: String, username: String, password: String, region: String, log: Logger): Option[Cognito.AuthTokens] = {
    import com.amazonaws.auth.profile.ProfileCredentialsProvider
    import com.amazonaws.auth.{ AWSCredentials, AWSCredentialsProvider }
    log.info(s"Getting id token for profile: '$profile' of region: '$region'")

    log.debug(
      s"""
         |Getting authentication tokens:
         |==============================
         |Profile: $profile
         |Region: $region
         |ClientId: $clientId
         |UserpoolId: $userPoolId
         |Username: $username
         |Password: $password
      """.stripMargin)

    def profileCredentialsProvider = {
      new AWSCredentialsProvider {
        override def refresh(): Unit = {}

        override def getCredentials: AWSCredentials = {
          val profileCredsProvider = new ProfileCredentialsProvider(profile)
          profileCredsProvider.getCredentials
        }
      }
    }

    def client = {
      AWSCognitoIdentityProviderClientBuilder
        .standard()
        .withRegion(region)
        .withCredentials(profileCredentialsProvider)
        .build()
    }

    def adminInitiateAuthRequest =
      new AdminInitiateAuthRequest()
        .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
        .withClientId(clientId)
        .withUserPoolId(userPoolId)
        .withAuthParameters(Map("USERNAME" -> username, "PASSWORD" -> password).asJava)

    val result: Try[AuthenticationResultType] = Try {
      client
        .adminInitiateAuth(adminInitiateAuthRequest)
        .getAuthenticationResult
    }

    result.map(result => Cognito.AuthTokens(result.getAccessToken, result.getExpiresIn, result.getTokenType, result.getRefreshToken, result.getIdToken))
      .recover {
        case t: Throwable =>
          log.error(s"Error while retrieving authentication tokens: ${t.getMessage}")
          throw t
      }.toOption
  }
}
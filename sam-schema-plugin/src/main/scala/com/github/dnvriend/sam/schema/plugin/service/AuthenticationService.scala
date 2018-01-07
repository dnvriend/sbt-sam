package com.github.dnvriend.sam.schema.plugin.service

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.github.dnvriend.sbt.aws.task.AwsCognitoIdpOperations

object AuthenticationService {
  def getIdToken(
                  userPoolId: String,
                  clientId: String,
                  username: String,
                  password: String,
                  client: AWSCognitoIdentityProvider,
                ): String = {

    AwsCognitoIdpOperations.getIdToken(
      username,
      password,
      userPoolId,
      clientId,
      client
    )
  }
}

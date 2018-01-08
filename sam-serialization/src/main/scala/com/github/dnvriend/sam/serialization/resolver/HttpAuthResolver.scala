package com.github.dnvriend.sam.serialization.resolver

import com.amazonaws.services.cognitoidp.{AWSCognitoIdentityProvider, AWSCognitoIdentityProviderClientBuilder}
import com.amazonaws.services.cognitoidp.model.{AdminInitiateAuthRequest, AuthFlowType, AuthenticationResultType}
import com.typesafe.scalalogging.LazyLogging
import org.apache.avro.Schema

import scala.collection.JavaConverters._

class HttpAuthResolver(
                        url: String,
                        clientId: String,
                        userPoolId: String,
                        username: String,
                        password: String,
                        maximumCacheSize: Int = 50,
                        expireAfterWriteSeconds: Int = 60,
                      ) extends SchemaResolver with LazyLogging {
  logger.info(
    """
      |Creating HttpAuthResolver:
      |==========================
      |url: '{}'
      |clientId: '{}'
      |userPoolId: '{}'
      |username: '{}'
      |password: '{}'
      |maximumCacheSize: '{}'
      |expireAfterWriteSeconds: '{}'
    """.stripMargin,
    url,
    clientId,
    userPoolId,
    username,
    password,
    maximumCacheSize,
    expireAfterWriteSeconds
  )
  private val client: AWSCognitoIdentityProvider = {
    AWSCognitoIdentityProviderClientBuilder.defaultClient()
  }

  /**
   * Returns the ID token, that must be used with the Authorization header
   */
  private def getIdToken: String = {
    logger.info("Getting Id Token")
    val result: AuthenticationResultType = {
      client
        .adminInitiateAuth(new AdminInitiateAuthRequest()
          .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
          .withClientId(clientId)
          .withUserPoolId(userPoolId)
          .withAuthParameters(Map("USERNAME" -> username, "PASSWORD" -> password).asJava)
        ).getAuthenticationResult
    }
    result.getIdToken
  }

  private val resolver: SchemaResolver = new HttpResolver(url, getIdToken, maximumCacheSize, expireAfterWriteSeconds)

  override def resolve(fingerprint: String): Option[Schema] = {
    logger.info("Getting schema for fingerprint: '{}'", fingerprint)
    resolver.resolve(fingerprint)
  }
}
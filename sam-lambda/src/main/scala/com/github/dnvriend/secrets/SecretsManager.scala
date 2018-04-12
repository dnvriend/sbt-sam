package com.github.dnvriend.secrets

import com.amazonaws.services.secretsmanager.model.{ GetSecretValueRequest, GetSecretValueResult }
import com.amazonaws.services.secretsmanager.{ AWSSecretsManager, AWSSecretsManagerClientBuilder }
import play.api.libs.json.Json
import scalaz.Scalaz._

import scala.util.Try

/**
 * AWS Secrets Manager is a web service that enables you to store, manage, and retrieve, secrets.
 */
object SecretsManager {
  /**
   * Returns the AWS Secrets Manager client.
   */
  def client(): AWSSecretsManager = {
    AWSSecretsManagerClientBuilder.defaultClient()
  }

  /**
   * Retrieves the contents of the encrypted fields SecretString and SecretBinary from the specified
   * version of a secret.
   */
  def getSecretValue(secretId: String, client: AWSSecretsManager): Try[GetSecretValueResult] = Try {
    client.getSecretValue(
      new GetSecretValueRequest().withSecretId(secretId)
    )
  }

  def getSecretValueOpt(secretId: String, client: AWSSecretsManager): Option[GetSecretValueResult] = {
    val result = getSecretValue(secretId, client).toDisjunction
    if (result.isLeft) {
      println(result.swap.map(_.getMessage).intercalate(""))
    }
    result.toOption
  }

  /**
   * The decrypted part of the protected secret information that was originally provided as a string.
   */
  def getSecretString(secretId: String, client: AWSSecretsManager): Option[String] = {
    getSecretValueOpt(secretId, client).map(_.getSecretString)
  }

  /**
   * The decrypted part of the protected secret information that was originally provided as binary data in the
   * form of a byte array. The response parameter represents the binary data as a base64-encoded string.
   */
  def getSecretBinary(secretId: String, client: AWSSecretsManager): Option[Array[Byte]] = {
    getSecretValueOpt(secretId, client).map(_.getSecretBinary).map(_.array())
  }

  def getDatabaseSecret(secretId: String, client: AWSSecretsManager): Option[DatabaseSecret] = {
    getSecretString(secretId, client).map(Json.parse).flatMap(_.asOpt[DatabaseSecret])
  }

  def getSecretAsMap(secretId: String, client: AWSSecretsManager): Option[Map[String, String]] = {
    getSecretString(secretId, client).map(Json.parse).map(_.as[Map[String, String]])
  }

}

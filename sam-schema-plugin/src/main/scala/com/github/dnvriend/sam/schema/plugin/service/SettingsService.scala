package com.github.dnvriend.sam.schema.plugin.service

object SettingsService {
  def schemaUrl(schemaUrlSetting: Option[String]): String = {
    sys.env.get("SAM_SCHEMA_URL")
      .orElse(schemaUrlSetting)
      .getOrElse(error("SAM_SCHEMA_URL or schemaRepositoryUrl not defined"))
  }

  def userPoolId(userPoolId: Option[String]): String = {
    sys.env.get("SAM_SCHEMA_USER_POOL_ID")
      .orElse(userPoolId)
      .getOrElse(error("SAM_SCHEMA_USER_POOL_ID or schemaUserPoolId not defined"))
  }

  def clientId(clientId: Option[String]): String = {
    sys.env.get("SAM_SCHEMA_CLIENT_ID")
      .orElse(clientId)
      .getOrElse(error("SAM_SCHEMA_CLIENT_ID or schemaUserPoolId not defined"))
  }

  def username(username: Option[String]): String = {
    sys.env.get("SAM_SCHEMA_USER_NAME")
      .orElse(username)
      .getOrElse(error("SAM_SCHEMA_USER_NAME or schemaUsername not defined"))
  }

  def password(password: Option[String]): String = {
    sys.env.get("SAM_SCHEMA_PASSWORD")
      .orElse(password)
      .getOrElse(error("SAM_SCHEMA_PASSWORD or schemaUsername not defined"))
  }

  def error[A](message: String): A = {
    throw new IllegalStateException(message)
  }
}

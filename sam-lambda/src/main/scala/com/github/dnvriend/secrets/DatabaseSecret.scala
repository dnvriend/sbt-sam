package com.github.dnvriend.secrets

import play.api.libs.json.{ Format, Json }

object DatabaseSecret {
  implicit val format: Format[DatabaseSecret] = Json.format
}
case class DatabaseSecret(
    username: String,
    password: String,
    engine: String,
    host: String,
    port: Int,
    dbname: String,
    dbInstanceIdentifier: String
)

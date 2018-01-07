package com.github.dnvriend.sam.serialization
package resolver

import org.apache.avro.Schema

import scalaj.http._
import scalaz.Scalaz._

class HttpResolver(url: String, authToken: String) extends SchemaResolver {

  override def resolve(fingerprint: String): Option[Schema] = {
    val result = {
      Http(s"$url/fingerprint/$fingerprint")
        .headers("Authorization" -> authToken)
        .timeout(Int.MaxValue, Int.MaxValue)
        .asString
    }.safe

    if (result.isLeft) {
      println(result.swap.foldMap(_.getMessage))
    }

    result.map(_.body).map(parseAvro).toOption
  }

  def parseAvro(schema: String): org.apache.avro.Schema = {
    new org.apache.avro.Schema.Parser().parse(schema)
  }
}


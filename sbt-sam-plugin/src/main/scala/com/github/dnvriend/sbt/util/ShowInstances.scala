package com.github.dnvriend.sbt.util

import scalaz.Show

trait ShowInstances {
  implicit val showThrowable: Show[Throwable] = Show.shows(error => s"Error, reason: ${error.getMessage}")
  implicit val showAvroSchema: Show[org.apache.avro.Schema] = Show.showFromToString
}
object ShowInstances extends ShowInstances

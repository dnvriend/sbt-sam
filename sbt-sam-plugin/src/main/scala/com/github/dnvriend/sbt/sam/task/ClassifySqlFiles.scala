package com.github.dnvriend.sbt.sam.task

import com.github.dnvriend.ops.AllOps
import play.api.libs.json.{ Json, Reads }
import sbt._

object ClassifySqlFiles extends AllOps {

  object ParsedSqlApplicationType {
    implicit val reads: Reads[ParsedSqlApplicationType] = Json.reads
  }

  final case class ParsedSqlApplicationType(Type: String)

  final case class ParsedSqlApplication(json: String, body: String, applicationType: ParsedSqlApplicationType)

  sealed trait SqlApplication

  object KinesisAnalyticsDetails {
    implicit val reads: Reads[KinesisAnalyticsDetails] = Json.reads
  }

  final case class KinesisAnalyticsDetails(
      Type: String,
      ApplicationName: String,
      ApplicationDescription: String,
      KinesisStreamsInput: String
  )

  final case class KinesisAnalytics(
      details: KinesisAnalyticsDetails,
      applicationCode: String
  ) extends SqlApplication

  def readSql(file: File): ParsedSqlApplication = {
    println("Reading sql file: " + file.absolutePath)
    val lines: List[String] = IO.read(file).split("\n").toList
    val json = lines.takeWhile(_ != "*/").filterNot(_ == "/*").mkString
    val body = lines.dropWhile(_ != "*/").filterNot(_ == "*/").mkString
    val sqlApplicationType: ParsedSqlApplicationType = Json.parse(json).as[ParsedSqlApplicationType]
    ParsedSqlApplication(json, body, sqlApplicationType)
  }

  def classifyApplication(parsed: ParsedSqlApplication): SqlApplication = parsed.applicationType.Type match {
    case "AWS::KinesisAnalytics::Application" =>
      val details = Json.parse(parsed.json).as[KinesisAnalyticsDetails]
      KinesisAnalytics(details, parsed.body)
  }

  def run(sqlFiles: Set[File], log: Logger): List[SqlApplication] = {
    sqlFiles
      .map(readSql)
      .map(classifyApplication)
      .toList
  }
}

package com.github.dnvriend.lambda

import java.io.InputStream
import java.sql.Timestamp
import java.text.SimpleDateFormat

import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json._

object CloudWatchEvent {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(DashSeparated)

  implicit object timestampFormat extends Reads[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    override def reads(json: JsValue): JsResult[Timestamp] = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
  }

  implicit val reads: Reads[CloudWatchEvent] = Json.reads

  def parse(input: InputStream): CloudWatchEvent =
    Json.parse(input).as[CloudWatchEvent]
}

case class CloudWatchEvent(
                            source: String,
                            account: String,
                            time: Timestamp,
                            region: String,
                            detail: CloudWatchDetails,
                            detailType: String,
                          )

object CloudWatchDetails {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(DashSeparated)
  implicit val reads: Reads[CloudWatchDetails] = Json.reads
}

case class CloudWatchDetails(
                              buildStatus: String,
                              projectName: String,
                              buildId: String,
                              additionalInformation: CloudWatchAdditionalInformation
                            )

object CloudWatchAdditionalInformation {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(DashSeparated)
  implicit val reads: Reads[CloudWatchAdditionalInformation] = Json.reads
}

case class CloudWatchAdditionalInformation(
                                            timeoutInMinutes: Int,
                                            buildComplete: Boolean,
                                            initiator: String,
                                            buildStartTime: String,
                                            phases: List[CloudWatchPhases],
                                            logs: CloudWatchLogs
                                          )

object CloudWatchPhases {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(DashSeparated)
  implicit val reads: Reads[CloudWatchPhases] = Json.reads
}

case class CloudWatchPhases(
                             phaseContext: Option[List[String]],
                             startTime: String,
                             endTime: Option[String],
                             durationInSeconds: Option[Int],
                             phaseType: String,
                             phaseStatus: Option[String]
                           )

object CloudWatchLogs {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(DashSeparated)
  implicit val writes: Reads[CloudWatchLogs] = Json.reads
}

case class CloudWatchLogs(
                           groupName: String,
                           streamName: String,
                           deepLink: String
                         )

/**
  * For each class property, use the dash separated equivalent
  * to name its column (e.g. fooBar -> foo-bar and vice versa).
  */
object DashSeparated extends JsonNaming {
  override def apply(property: String): String = {
    val length = property.length
    val result = new StringBuilder(length * 2)
    var resultLength = 0
    var wasPrevTranslated = false
    for (i <- 0 until length) {
      var c = property.charAt(i)
      if (i > 0 || i != '-') {
        if (Character.isUpperCase(c)) {
          // append a underscore if the previous result wasn't translated
          if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != '-') {
            result.append('-')
            resultLength += 1
          }
          c = Character.toLowerCase(c)
          wasPrevTranslated = true
        } else {
          wasPrevTranslated = false
        }
        result.append(c)
        resultLength += 1
      }
    }

    // builds the final string
    result.toString()
  }

  override val toString = "DashSeparated"
}
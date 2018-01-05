package com.github.dnvriend.sbt.sam.cf.resource.kanalytics

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{Json, Writes}

import scalaz.NonEmptyList
import scalaz.syntax.all._

object CFKinesisStreamsOutput {
  implicit val writes: Writes[CFKinesisStreamsOutput] = Writes.apply(model => {
    import model._
    Json.obj(
      "ResourceARN" -> resourceARN,
      "RoleARN" -> roleARN
    )
  })
}

/**
  * The KinesisStreamsOutput property type specifies an Amazon Kinesis stream as the destination
  * when you are configuring application output.
  */
case class CFKinesisStreamsOutput(
                                   /**
                                     * The Amazon Resource Name (ARN) of the destination Amazon Kinesis stream to write to.
                                     */
                                   resourceARN: String,

                                   /**
                                     * The ARN of the IAM role that Amazon Kinesis Data Analytics can assume to write
                                     * to the destination stream on your behalf.
                                     */
                                   roleARN: String,
                                 )

object CFKinesisFirehoseOutput {
  implicit val writes: Writes[CFKinesisFirehoseOutput] = Writes.apply(model => {
    import model._
    Json.obj(
      "ResourceARN" -> resourceARN,
      "RoleARN" -> roleARN
    )
  })
}

/**
  * The KinesisFirehoseOutput property type specifies an Amazon Kinesis Data Firehose delivery stream
  * as the destination when you are configuring application output.
  */
case class CFKinesisFirehoseOutput(
                                    /**
                                      * The Amazon Resource Name (ARN) of the destination
                                      * Amazon Kinesis Data Firehose delivery stream to write to.
                                      */
                                    resourceARN: String,

                                    /**
                                      * The ARN of the IAM role that Amazon Kinesis Data Analytics can assume to write
                                      * to the destination stream on your behalf.
                                      */
                                    roleARN: String,
                                  )

object CFDestinationSchema {
  implicit val writes: Writes[CFDestinationSchema] = Writes.apply(model => {
    import model._
    Json.obj("RecordFormatType" -> recordFormatType)
  })
}

/**
  * The DestinationSchema property describes the data format when records are written to the destination.
  */
case class CFDestinationSchema(
                                /**
                                  * Specifies the format of the records on the output stream.
                                  */
                                  recordFormatType: String
                              )

object CFOutput {
  implicit val writes: Writes[CFOutput] = Writes.apply(model => {
    import model._
    NonEmptyList(
      Json.obj("Name" -> name),
      Json.obj("DestinationSchema" -> destinationSchema),
      Json.toJson(kinesisFirehoseOutput.map(value => Json.obj("KinesisFirehoseOutput" -> value))),
      Json.toJson(kinesisStreamsOutput.map(value => Json.obj("KinesisStreamsOutput" -> kinesisStreamsOutput))),
    ).fold(JsMonoids.jsObjectMerge)
  })
}

/**
  * The Output property type specifies an array of output configuration objects
  * for an Amazon Kinesis Data Analytics application.
  */
case class CFOutput(
                     /**
                       * The name of the in-application stream.
                       */
                      name: String,

                     /**
                       * The data format when records are written to the destination.
                       */
                      destinationSchema: CFDestinationSchema,

                     /**
                       * Identifies an Amazon Kinesis Data Firehose delivery stream as the destination.
                       */
                      kinesisFirehoseOutput: Option[CFKinesisFirehoseOutput],

                     /**
                       * Identifies an Amazon Kinesis stream as the destination.
                       */
                     kinesisStreamsOutput: Option[CFKinesisStreamsOutput],
)

object CFApplicationOutput {
  implicit val writes: Writes[CFApplicationOutput] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::KinesisAnalytics::ApplicationOutput",
        "DependsOn" -> dependsOn,
        "Properties" -> Json.obj(
          "ApplicationName" -> applicationName,
          "Output" -> output
        )
      )
    )
  })
}

/**
  * The AWS::KinesisAnalytics::ApplicationOutput resource adds an external destination to your
  * Amazon Kinesis Data Analytics application. For more information, see AddApplicationOutput
  * in the Amazon Kinesis Data Analytics Developer Guide.
  */
case class CFApplicationOutput(
                              logicalName: String,

                              /**
                                * logicalName of the Kinesis Analytics Application
                                */
                              dependsOn: String,

                              /**
                                * The name of the application to which you want to add the output configuration.
                                */
                              applicationName: String,

                              /**
                                * Describes the output configuration.
                                */
                              output: CFOutput,
                              )
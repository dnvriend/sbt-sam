package com.github.dnvriend.sbt.sam.cf.resource.kanalytics

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{Json, Writes}

import scalaz.NonEmptyList
import scalaz.syntax.all._

object CFInputLambdaProcessor {
  implicit val writes: Writes[CFInputLambdaProcessor] = Writes.apply(model => {
    import model._
    Json.obj()
  })
}

case class CFInputLambdaProcessor(
resourceARN : String,
roleARN: String
                                 )

object CFInputProcessingConfiguration {
  implicit val writes: Writes[CFInputProcessingConfiguration] = Writes.apply(model => {
    import model._
    Json.obj("InputLambdaProcessor" -> inputLambdaProcessor)
  })
}

/**
  * The InputProcessingConfiguration property type specifies a processing configuration
  */
case class CFInputProcessingConfiguration(
                                           /**
                                             * The InputLambdaProcessor that is used to preprocess the records
                                             * in the stream before they are processed by your application code.
                                             */
                                            inputLambdaProcessor: CFInputLambdaProcessor
                                         )

object CFKinesisStreamsInput {
  implicit val writes: Writes[CFKinesisStreamsInput] = Writes.apply(model => {
    import model._
    Json.obj(
      "ResourceARN" -> resourceARN,
      "RoleARN" -> roleARN
    )
  })
}

/**
  * The KinesisStreamsInput property type specifies an Amazon Kinesis stream
  * as the streaming source for an Amazon Kinesis Data Analytics application.
  */
case class CFKinesisStreamsInput(
                                  /**
                                    * The Amazon Resource Name (ARN) of the input Amazon Kinesis stream to read.
                                    */
                                  resourceARN: String,

                                  /**
                                    * The ARN of the IAM role that Kinesis Data Analytics can assume
                                    * to access the stream on your behalf.
                                    */
                                  roleARN: String
                                )

object CFKinesisFirehoseInput {
  implicit val writes: Writes[CFKinesisFirehoseInput] = Writes.apply(model => {
    import model._
    Json.obj(
      "ResourceARN" -> resourceARN,
      "RoleARN" -> roleARN
    )
  })
}

/**
  * The KinesisFirehoseInput property type identifies an Amazon Kinesis Data Firehose delivery
  * stream as the streaming source for an Amazon Kinesis Data Analytics application.
  */
case class CFKinesisFirehoseInput(
                                   /**
                                     * The Amazon Resource Name (ARN) of the input Kinesis Firehose delivery stream.
                                     */
                                  resourceARN: String,

                                   /**
                                     * The ARN of the IAM role that Kinesis Data Analytics can assume to access the stream on your behalf.
                                     */
                                  roleARN: String
                                 )

object CFInputSchema {
  implicit val writes: Writes[CFInputSchema] = Writes.apply(model => {
    import model._
    Json.obj(
      "RecordColumns" -> recordColumns,
      "RecordEncoding" -> recordEncoding,
      "RecordFormat" -> recordFormat
    )
  })
}

/**
  * The InputSchema property type describes the format of the data in the streaming source, and how each
  * data element maps to corresponding columns that are created in the in-application stream in an Amazon
  * Kinesis Data Analytics application.
  */
case class CFInputSchema(
                        /**
                          * A list of RecordColumn objects.
                          */
                       recordColumns: List[CFRecordColumn],

                        /**
                          * Specifies the encoding of the records in the streaming source; for example, UTF-8.
                          */
                        recordEncoding: String,

                        /**
                          * Specifies the format of the records on the streaming source.
                          */
                        recordFormat: CFRecordFormat
                        )

object CFInputParallelism {
  implicit val writes: Writes[CFInputParallelism] = Writes.apply(model => {
    import model._
    Json.obj("Count" -> count)
  })

  final val DefaultInputParallism: CFInputParallelism = {
    CFInputParallelism(1)
  }
}

/**
  * The InputParallelism property type specifies the number of in-application streams to create
  * for a given streaming source in an Amazon Kinesis Data Analytics application.
  */
case class CFInputParallelism(
                               /**
                                 * The number of in-application streams to create.
                                 */
                               count: Int = 1,
                             )

object CFInput {
  implicit val writes: Writes[CFInput] = Writes.apply(model => {
    import model._
    NonEmptyList(
      Json.obj("NamePrefix" -> namePrefix),
      Json.obj("InputParallelism" -> inputParallelism),
      Json.obj("InputSchema" -> inputSchema),
      Json.toJson(kinesisFirehoseInput.map(value => Json.obj("KinesisFirehoseInput" -> value))),
      Json.toJson(kinesisStreamsInput.map(value => Json.obj("KinesisStreamsInput" -> value))),
      Json.obj("InputProcessingConfiguration" ->  inputProcessingConfiguration),
    ).fold(JsMonoids.jsObjectMerge)
  })
}

/**
  * When you configure the application input, you specify the streaming source, the in-application
  * stream name that is created, and the mapping between the two.
  */
case class CFInput(
                    /**
                      * The name prefix to use when creating the in-application streams.
                      */
                    namePrefix: String,

                    /**
                      * Describes the number of in-application streams to create.
                      */
                    inputParallelism: CFInputParallelism,

                    /**
                      * Describes the format of the data in the streaming source, and how each data element
                      * maps to corresponding columns in the in-application stream that is being created.
                      */
                    inputSchema: CFInputSchema,

                    /**
                      * If the streaming source is an Amazon Kinesis Firehose delivery stream, identifies the delivery
                      * stream's Amazon Resource Name (ARN) and an IAM role that enables Kinesis Data Analytics to
                      * access the stream on your behalf.
                      */
                    kinesisFirehoseInput: Option[CFKinesisFirehoseInput],

                    /**
                      * If the streaming source is an Amazon Kinesis stream, identifies the stream's ARN and an IAM
                      * role that enables Kinesis Data Analytics to access the stream on your behalf.
                      */
                    kinesisStreamsInput: Option[CFKinesisStreamsInput],

                    /**
                      * The input processing configuration for the input. An input processor transforms records as they are
                      * received from the stream, before the application's SQL code executes. Currently, the only input
                      * processing configuration available is 'InputLambdaProcessor'.
                      */
                    inputProcessingConfiguration: CFInputProcessingConfiguration,
                  )

object CFApplication {
  implicit val writes: Writes[CFApplication] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::KinesisAnalytics::Application",
        "Properties" -> Json.obj(
          "ApplicationName" -> applicationName,
          "ApplicationDescription" -> applicationDescription,
          "ApplicationCode" -> applicationCode,
          "Inputs" -> inputs
        )
      )
    )
  })
}

/**
  * The AWS::KinesisAnalytics::Application resource creates an Amazon Kinesis Data Analytics application.
  */
case class CFApplication(
                        logicalName: String,

                        /**
                          * The name of your Amazon Kinesis Data Analytics application.
                          */
                        applicationName: String,

                        /**
                          * The summary description of the application.
                          */
                        applicationDescription: String,

                        /**
                          * One or more SQL statements that read input data, transform it, and generate output.
                          */
                        applicationCode: String,

                        /**
                          * Use this parameter to configure the application input.
                          */
                        inputs: List[CFInput],
                        )
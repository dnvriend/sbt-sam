package com.github.dnvriend.sbt.sam.cf.resource.kanalytics

import com.github.dnvriend.sbt.util.JsMonoids
import play.api.libs.json.{Json, Writes}

import scalaz.NonEmptyList
import scalaz.syntax.all._


object CFJSONMappingParameters {
  implicit val writes: Writes[CFJSONMappingParameters] = Writes.apply(model => {
    import model._
    Json.obj("RecordRowPath" -> recordRowPath)
  })

  final val DefaultJsonMappingParameters: CFJSONMappingParameters = {
    CFJSONMappingParameters("$")
  }
}

/**
  * Provides additional mapping information when JSON is the record format on the streaming source.
  */
case class CFJSONMappingParameters(
                                    /**
                                      * Path to the top-level parent that contains the records (e.g., "$".)
                                      */
                                      recordRowPath: String = "$"
                                  )

object CFCSVMappingParameters {
  implicit val writes: Writes[CFCSVMappingParameters] = Writes.apply(model => {
    import model._
    Json.obj(
      "RecordColumnDelimiter" -> recordColumnDelimiter,
      "RecordRowDelimiter" -> recordRowDelimiter
    )
  })

  final val DefaultCsvMappingParameters: CFCSVMappingParameters = {
    CFCSVMappingParameters(",", "\n")
  }
}

/**
  * In AWS CloudFormation, use the CSVMappingParameters property to specify additional mapping information
  * when the record format uses delimiters, such as CSV.
  */
case class CFCSVMappingParameters(
                                   /**
                                     * The column delimiter. For example, in a CSV format, a comma (",") is the typical column delimiter.
                                     */
                                    recordColumnDelimiter: String = ",",

                                   /**
                                     * The row delimiter. For example, in a CSV format, "\n" is the typical row delimiter.
                                     */
                                    recordRowDelimiter: String = "\n",
)

object CFMappingParameters {
  implicit val writes: Writes[CFMappingParameters] = Writes.apply(model => {
    import model._
    NonEmptyList(
      Json.toJson(csvMappingParameters),
      Json.toJson(jsonMappingParameters),
    ).fold(JsMonoids.jsObjectMerge)
  })
}

/**
  * When configuring application input at the time of creating or updating an application, provides
  * additional mapping information specific to the record format (such as JSON, CSV, or record
  * fields delimited by some delimiter) on the streaming source.
  */
case class CFMappingParameters(
                            /**
                              * Provides additional mapping information when the record format uses delimiters (for example, CSV).
                              */
                            csvMappingParameters: Option[CFCSVMappingParameters],

                            /**
                              * Provides additional mapping information when JSON is the record format on the streaming source.
                              */
                            jsonMappingParameters: Option[CFJSONMappingParameters],
                            )

object CFRecordFormat {
  implicit val writes: Writes[CFRecordFormat] = Writes.apply(model => {
    import model._
    Json.obj(
      "MappingParameters" -> mappingParameters,
      "RecordFormatType" -> recordFormatType
    )
  })
}

/**
  * The RecordFormat property type specifies the record format and relevant mapping information that
  * should be applied to schematize the records on the stream.
  */
case class CFRecordFormat(
                           /**
                             * When configuring application input at the time of creating or updating an application,
                             * provides additional mapping information specific to the record format (such as JSON, CSV,
                             * or record fields delimited by some delimiter) on the streaming source.
                             */
                          mappingParameters : CFMappingParameters,

                           /**
                             * The type of record format (CSV or JSON).
                             */
                          recordFormatType: String
)

object CFRecordColumn {
  implicit val writes: Writes[CFRecordColumn] = Writes.apply(model => {
    import model._
    Json.obj(
      "Mapping" -> mapping,
      "Name" -> name,
      "SqlType" -> sqlType
    )
  })
}

/**
  * The RecordColumn property type specifies the mapping of each data element in the streaming source to the
  * corresponding column in the in-application stream.
  */
case class CFRecordColumn(
                           /**
                             * The reference to the data element in the streaming input of the reference data source.
                             */
                            mapping: String,

                           /**
                             * The name of the column created in the in-application input stream or reference table.
                             */
                            name: String,

                           /**
                             * The SQL data type of the column created in the in-application input stream or reference table.
                             */
                            sqlType: String,
)

object CFReferenceSchema {
  implicit val writes: Writes[CFReferenceSchema] = Writes.apply(model => {
    import model._
    Json.obj(
      "RecordColumns" -> recordColumns,
      "RecordEncoding" -> recordEncoding,
      "RecordFormat" -> recordFormat
    )
  })
}

/**
  * The ReferenceSchema property type specifies the format of the data in the streaming source,
  * and how each data element maps to corresponding columns created in the in-application stream.
  */
case class CFReferenceSchema(
  recordColumns: List[CFRecordColumn],
  recordEncoding: String,
  recordFormat: CFRecordFormat,
)

object CFS3ReferenceDataSource {
  implicit val writes: Writes[CFS3ReferenceDataSource] = Writes.apply(model => {
    import model._
    Json.obj(
      "BucketARN" -> bucketARN,
      "FileKey" -> fileKey,
      "ReferenceRoleARN" -> referenceRoleARN
    )
  })
}

/**
  * The S3ReferenceDataSource property type specifies the Amazon S3 bucket and object that contains the reference
  * data for Amazon Kinesis Data Analytics.
  */
case class CFS3ReferenceDataSource(
                                  /**
                                    * The Amazon Resource Name (ARN) of the Amazon S3 bucket.
                                    */
                                  bucketARN : String,

                                  /**
                                    * The object key name containing reference data.
                                    */
                                  fileKey : String,

                                  /**
                                    * The ARN of the IAM role that the service can assume
                                    * to read data on your behalf.
                                    */
                                  referenceRoleARN : String,
                                )


object CFReferenceDataSource {
  implicit val writes: Writes[CFReferenceDataSource] = Writes.apply(model => {
    import model._
    Json.obj(
      "TableName" -> tableName,
      "S3ReferenceDataSource" -> s3ReferenceDataSource,
      "ReferenceSchema" -> referenceSchema
    )
  })
}

/**
  * The ReferenceDataSource property type specifies the reference data source by providing the source information
  * (Amazon S3 bucket name and object key name), the resulting in-application table name that is created,
  * and the necessary schema to map the data elements in the Amazon S3 object to the in-application table.
  */
case class CFReferenceDataSource(
                                  /**
                                    * The name of the in-application table to create.
                                    */
                                  tableName: String,

                                  /**
                                    * Identifies the Amazon S3 bucket and object that contains the reference data.
                                    * Also identifies the IAM role that Amazon Kinesis Data Analytics can assume to read
                                    * this object on your behalf.
                                    */
                                  s3ReferenceDataSource: CFS3ReferenceDataSource,


                                  /**
                                    * Describes the format of the data in the streaming source, and how each data element maps
                                    * to corresponding columns that are created in the in-application stream.
                                    */
                                  referenceSchema: CFReferenceSchema,
                                )

object CFApplicationReferenceDataSource {
  implicit val writes: Writes[CFApplicationReferenceDataSource] = Writes.apply(model => {
    import model._
    Json.obj(
      logicalName -> Json.obj(
        "Type" -> "AWS::KinesisAnalytics::ApplicationReferenceDataSource",
        "DependsOn" -> dependsOn,
        "Properties" -> Json.obj(
          "ApplicationName" -> applicationName,
          "ReferenceDataSource" -> referenceDataSource
        )
      )
    )
  })
}

/**
  * Use the AWS CloudFormation AWS::KinesisAnalytics::ApplicationReferenceDataSource resource to add a reference
  * data source to an existing Amazon Kinesis Data Analytics application.
  */
case class CFApplicationReferenceDataSource(
                                           logicalName: String,

                                           /**
                                             * logicalName of the Kinesis Analytics Application
                                             */
                                           dependsOn: String,

                                           /**
                                             * The name of the kinesis analytics application
                                             */
                                           applicationName: String,

                                           /**
                                             * The reference data source, which is an object in your
                                             * Amazon Simple Storage Service (Amazon S3) bucket.
                                             */
                                           referenceDataSource: CFReferenceDataSource
                                           )
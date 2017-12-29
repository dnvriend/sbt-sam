package com.github.dnvriend.sbt.resource.glue.model

case class ConnectionInput(value: String)
case class CatalogId(value: String)
case class LocationUri(value: String)
case class Description(value: String)
case class Name(value: String)
case class DatabaseName(value: String)
case class Owner(value: String)
case class TableType(value: String)

/**
 * The DatabaseInput property type specifies the metadata that is used to create or update an AWS Glue database.
 */
case class DatabaseInput(locationUri: LocationUri, description: Description, name: Name)

/**
 * The TableInput property type specifies the metadata that's used to create or update an AWS Glue table.
 */
case class TableInput(owner: Owner, tableType: TableType, description: Description, name: Name)

/**
 * The AWS::Glue::Database resource specifies a logical grouping of tables in AWS Glue.
 */
case class Connection(connectionInput: ConnectionInput, catalogId: CatalogId)
/**
 * The AWS::Glue::Database resource specifies a logical grouping of tables in AWS Glue
 */
case class Database(databaseInput: DatabaseInput, catalogId: CatalogId)

/**
 * The AWS::Glue::Table resource specifies tabular data in the AWS Glue data catalog.
 */
case class Table(catalogId: CatalogId, databaseName: DatabaseName, tableInput: TableInput)
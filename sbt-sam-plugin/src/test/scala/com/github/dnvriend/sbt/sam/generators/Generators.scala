package com.github.dnvriend.sbt.sam.generators

object Generators extends Generators
trait Generators extends GenCFDynamoDBTable
  with GenAuthpool
  with GenKinesisStream
  with GenTopic
  with GenGeneric
  with GenLambdaHandler
  with GenTableWithIndex
  with GenS3Bucket
  with GenS3Firehose
  with GenIamRole
  with GenProjectConfiguration
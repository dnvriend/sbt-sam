package com.github.dnvriend.sbt.sam.generators

import com.github.dnvriend.sbt.sam.resource.kinesis.model.KinesisStream
import org.scalacheck.{ Arbitrary, Gen }

trait GenKinesisStream extends GenGeneric {
  val genKinesisStream = for {
    name <- Gen.const("kinesis-stream-name")
    configName <- genResourceConfName
    retensionPeriodHours <- Gen.posNum[Int]
    shardCount <- Gen.posNum[Int]
  } yield KinesisStream(name, configName, retensionPeriodHours, shardCount, true)

  implicit val arbKinesisStream: Arbitrary[KinesisStream] = Arbitrary.apply(genKinesisStream)

  val iterKinesisStream: Iterator[KinesisStream] = iterFor(genKinesisStream)
}
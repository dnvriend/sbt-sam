package com.github.dnvriend.sbt.sam.generators

import com.github.dnvriend.sbt.sam.task._
import org.scalacheck.{ Arbitrary, Gen }

trait GenLambdaHandler extends GenGeneric {
  val genLambdaConf = for {
    fqcn <- Gen.const("fqcn")
    simpleClassName <- genResourceConfName
    memorySize <- Gen.const(1024)
    timeout <- Gen.const(300)
    description <- genAlphaNonEmpty
  } yield LambdaConfig(
    fqcn,
    simpleClassName,
    memorySize,
    timeout,
    description
  )
  val genHttpHandler = for {
    lambdaConf <- genLambdaConf
    path <- Gen.uuid.map(id => s"/id/$id")
    method <- Gen.oneOf("get", "post", "put", "patch", "delete", "head")
  } yield HttpHandler(
    lambdaConf,
    HttpConf(
      path,
      method
    )
  )
  implicit val arbHttpHandler: Arbitrary[HttpHandler] = Arbitrary.apply(genHttpHandler)

  val iterHttpHandler: Iterator[HttpHandler] = iterFor(genHttpHandler)

  val genDynamoHandler = for {
    lambdaConf <- genLambdaConf
  } yield DynamoHandler(
    lambdaConf,
    DynamoConf()
  )

  val iterDynamoHandler: Iterator[DynamoHandler] = iterFor(genDynamoHandler)

  val genScheduledEventHandler = for {
    lambdaConf <- genLambdaConf
    schedule <- Gen.alphaStr
  } yield ScheduledEventHandler(
    lambdaConf,
    ScheduleConf(schedule)
  )

  val iterScheduledEventHandler: Iterator[ScheduledEventHandler] = iterFor(genScheduledEventHandler)

  val genSNSEventHandler = for {
    lambdaConf <- genLambdaConf
    topic <- Gen.alphaStr
  } yield SNSEventHandler(
    lambdaConf,
    SNSConf(topic)
  )

  val iterSNSEventHandler: Iterator[SNSEventHandler] = iterFor(genSNSEventHandler)

  val genKinesisEventHandler = for {
    lambdaConf <- genLambdaConf
    stream <- Gen.alphaStr
  } yield KinesisEventHandler(
    lambdaConf,
    KinesisConf(stream)
  )

  val iterKinesisEventHandler: Iterator[KinesisEventHandler] = iterFor(genKinesisEventHandler)
}
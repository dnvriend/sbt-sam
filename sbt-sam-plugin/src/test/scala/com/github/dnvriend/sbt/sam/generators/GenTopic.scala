package com.github.dnvriend.sbt.sam.generators

import com.github.dnvriend.sbt.sam.resource.sns.model.Topic
import org.scalacheck.{ Arbitrary, Gen }

trait GenTopic extends GenGeneric {
  val genTopic = for {
    name <- Gen.const("snsTopicName")
    configName <- genResourceConfName
    displayName <- Gen.const("displayName")
    topicName <- Gen.const("topicName")
  } yield Topic(name, configName, displayName, true)

  implicit val arbTopic: Arbitrary[Topic] = Arbitrary.apply(genTopic)

  val iterTopic: Iterator[Topic] = iterFor(genTopic)
}
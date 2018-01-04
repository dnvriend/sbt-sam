package com.github.dnvriend.sbt.sam.generators

import org.scalacheck.Gen

trait GenGeneric {
  val genResourceConfName = for {
    logicalName <- Gen.const("ResourceConfigurationName").flatMap(name => Gen.uuid.map(id => s"$name-$id"))
  } yield logicalName

  val genAlphaNonEmpty = for {
    value <- Gen.alphaStr.suchThat(_.nonEmpty)
  } yield value

  def iterFor[A](gen: Gen[A]): Iterator[A] = Stream.continually(gen.sample).collect { case Some(x) => x }.iterator
}
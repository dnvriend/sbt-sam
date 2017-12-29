package com.github.dnvriend.ops

import scalaz.Disjunction

trait ConverterOps {
  implicit def ConvertToConvOps[A](that: A): ToConverterOps[A] = new ToConverterOps[A](that)
}

class ToConverterOps[A](that: A) {
  def conv[B](implicit converter: Converter[A, B]): B = converter(that)
  def safeConv[B](implicit converter: Converter[A, B]): Disjunction[Throwable, B] = {
    Disjunction.fromTryCatchNonFatal(converter(that))
  }
}

object Converter {
  def apply[A, B](implicit conv: Converter[A, B]): Converter[A, B] = conv
  def instance[A, B](f: A => B): Converter[A, B] = new Converter[A, B] {
    override def apply(a: A): B = f(a)
  }
}
trait Converter[A, B] extends Function1[A, B]
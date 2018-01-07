package com.github.dnvriend.sam.schema

import scalaz._
import scalaz.Scalaz._

package object plugin {
  type DisjunctionNel[A, B] = Disjunction[NonEmptyList[A], B]
  type DTry[A] = Disjunction[Throwable, A]

  def success[A](value: A): DTry[A] = \/-(value)
  def failure[A](value: Throwable): DTry[A] = -\/(value)

  implicit def toDisjunctionOps[A](f: => A): DisjunctionSafeOps[A] = new DisjunctionSafeOps(f)
  class DisjunctionSafeOps[A](f: => A) {
    def safe: DTry[A] = Disjunction.fromTryCatchNonFatal(f)
  }
  implicit class OptionOps[A](val that: Option[A]) {
    def orFail(msg: String): DTry[A] = that.toRightDisjunction(new RuntimeException(msg))
  }

  case class SamSchema(fqcn: String, schema: String)
}

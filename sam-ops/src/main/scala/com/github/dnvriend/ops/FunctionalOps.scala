// Copyright 2017 Dennis Vriend
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.dnvriend.ops

import com.github.dnvriend.ops.DisjunctionNel.{ DisjunctionNel, Nel }

import scala.util.Try
import scalaz._
import scalaz.Scalaz._

object DisjunctionNel {
  type Nel[A] = NonEmptyList[A]
  type DisjunctionNel[A, B] = Disjunction[Nel[A], B]
  def lift[A, B](a: B): DisjunctionNel[A, B] = a.right[Nel[A]]
  def fromTryCatchNonFatal[T](t: => T): DisjunctionNel[Throwable, T] = {
    Disjunction.fromTryCatchNonFatal(t).leftMap(_.wrapNel)
  }
  def fromTry[A](that: Try[A]): Disjunction[Throwable, A] = that match {
    case scala.util.Success(a) => \/-(a)
    case scala.util.Failure(t) => -\/(t)
  }
}

trait FunctionalOps extends OutputStreamOps {
  implicit def ToFunctionalOpsImpl[A](that: => A) = new FunctionalOpsImpl(that)

  def fromTryNel[A](that: Try[A]): DisjunctionNel[Throwable, A] = {
    DisjunctionNel.fromTry(that).leftMap(_.wrapNel)
  }

  implicit val throwableShow: Show[Throwable] = Show.shows[Throwable](t => {
    withPrintWriter(pw => t.printStackTrace(pw))
  })

  implicit def nelShow[A](nel: Nel[A])(implicit ashow: Show[A], s: Semigroup[String]) = Show.shows[Nel[A]](nel => nel.map(ashow.shows).intercalate(","))

  implicit def disShow[A, B](x: Disjunction[A, B])(implicit ashow: Show[A], bshow: Show[B]) = Show.shows[Disjunction[A, B]](dis => dis.fold(a => ashow.shows(a), b => bshow.shows(b)))

  implicit def disShowNel[A, B](x: DisjunctionNel[A, B])(implicit ashow: Show[Nel[A]], bshow: Show[B]) = Show.shows[DisjunctionNel[A, B]](dis => dis.fold(a => ashow.shows(a), b => bshow.shows(b)))
}

class FunctionalOpsImpl[A](that: => A) {
  def safe: Disjunction[Throwable, A] = Disjunction.fromTryCatchNonFatal(that)
  def safeNel: DisjunctionNel[Throwable, A] = Disjunction.fromTryCatchNonFatal(that).leftMap(_.wrapNel)
  def ? : Option[A] = Option(that)
  def log(implicit show: Show[A] = null): A = {
    val msg: String = Option(show).map(_.shows(that)).getOrElse(that.toString)
    println(msg)
    that
  }
}

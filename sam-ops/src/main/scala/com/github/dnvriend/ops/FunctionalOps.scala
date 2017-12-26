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
}

trait FunctionalOps extends OutputStreamOps {
  implicit def ToFunctionalOpsImpl[A](that: => A) = new FunctionalOpsImpl(that)
}

class FunctionalOpsImpl[A](that: => A) {
  def safe: Disjunction[Throwable, A] = Disjunction.fromTryCatchNonFatal(that)
  def ? : Option[A] = Option(that)
  def log(implicit show: Show[A] = null): A = {
    val msg: String = Option(show).map(_.shows(that)).getOrElse(that.toString)
    println(msg)
    that
  }
}

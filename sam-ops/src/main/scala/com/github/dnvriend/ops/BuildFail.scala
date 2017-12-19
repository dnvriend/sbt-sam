package com.github.dnvriend.ops

import scalaz.Scalaz._
import scalaz._

trait BuildFail {
  /**
   * Fails the build by throwing a BuildFailedException with a message
   */
  def failureMessage[F[_]: Functor: Foldable, A](messages: F[A])(implicit show: Show[A]): String = {
    messages.map(show.shows).intercalate(",")
  }

  implicit class BuildFailOpsF[F[_]: Foldable: Functor, A: Show: Monoid, B](that: Disjunction[F[A], B]) {
    def getOrFail(): B = {
      if (that.isLeft) {
        throw new RuntimeException("Build Failed: " + that.swap.map(x => failureMessage(x)).getOrElse(""))
      } else {
        that.getOrElse(throw new RuntimeException(""))
      }
    }
  }

  implicit class BuildFailOpsString[B](that: Disjunction[String, B]) {
    def getOrFail(): B = {
      if (that.isLeft) {
        throw new RuntimeException("Build Failed: " + that.swap.getOrElse(""))
      } else {
        that.getOrElse(throw new RuntimeException(""))
      }
    }
  }
}

object BuildFail extends BuildFail
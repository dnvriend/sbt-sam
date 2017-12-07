package com.github.dnvriend.sbt.util

import scalaz._

object Report extends ShowInstances {
  /**
   * Given a list of records, generate a report
   */
  def report[F[_], A](xs: F[A])(implicit show: Show[A], m: Monoid[String], fold: Foldable[F], functor: Functor[F]): String = {
    val ys: F[String] = functor.map(xs)(show.shows)
    fold.intercalate(ys, "\n")
  }

  /**
   * Given a list of records, or an error, generate a report
   */
  def report[F[_]: Foldable: Functor, A: Show](co: Disjunction[Throwable, F[A]])(implicit showT: Show[Throwable], m: Monoid[String]): String = {
    co.bimap(showT.shows, xs => report(xs)).merge
  }
}

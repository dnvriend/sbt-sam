package com.github.dnvriend.sbt.aws.task

import scalaz.Show

/**
 * Progress bar code borrowed from https://github.com/sbt/sbt-s3/blob/master/src/main/scala/S3Plugin.scala
 */
object ProgressBar {
  implicit val show: Show[ProgressBar] = Show.shows(model => {
    val b = "=================================================="
    val s = "                                                  "
    val p = model.percent / 2
    val z: StringBuilder = new StringBuilder(80)
    z.append("\r[")
    z.append(b.substring(0, p))
    if (p < 50) { z.append("=>"); z.append(s.substring(p)) }
    z.append("]   ")
    if (p < 5) z.append(" ")
    if (p < 50) z.append(" ")
    z.append(model.percent)
    z.append("%   ")
    z.mkString
  })
}
final case class ProgressBar(percent: Int)

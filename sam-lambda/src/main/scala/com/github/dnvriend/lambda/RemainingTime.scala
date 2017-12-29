package com.github.dnvriend.lambda

import scala.compat.Platform

trait RemainingTime {
  private var invocations: List[Long] = List.empty
  private def averageInvocationTime(): Long = {
    if (invocations.isEmpty) 0L else invocations.sum / invocations.length
  }
  def invokeRemaining[A](remainingTimeInMillis: Long)(f: => A): A = {
    val average: Long = averageInvocationTime()
    if (remainingTimeInMillis > average) {
      val start: Long = Platform.currentTime
      val value: A = f
      val end: Long = Platform.currentTime
      val duration: Long = end - start
      invocations = invocations :+ duration
      println(s"Invocation duration: '$duration' ms, average: '${averageInvocationTime()}' ms, remaining: '${remainingTimeInMillis - duration}' ms")
      value
    } else throw new RuntimeException(s"Cannot invoke function, remaining invocation time: '$remainingTimeInMillis' ms, average invocation time is: '$average' ms")
  }
}

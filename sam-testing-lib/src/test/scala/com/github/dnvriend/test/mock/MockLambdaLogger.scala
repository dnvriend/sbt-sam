package com.github.dnvriend.test.mock

import com.amazonaws.services.lambda.runtime.LambdaLogger

object MockLambdaLogger {
  def apply(): LambdaLogger = new MockLambdaLogger
}
class MockLambdaLogger extends LambdaLogger {
  override def log(message: String): Unit = {
    println(message)
  }

  override def log(message: Array[Byte]): Unit = {
    println(new String(message), "UTF-8")
  }
}

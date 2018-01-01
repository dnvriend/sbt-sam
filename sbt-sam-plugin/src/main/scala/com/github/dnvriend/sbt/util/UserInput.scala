package com.github.dnvriend.sbt.util

import sbt._

object UserInput {
  def readInput(prompt: String): String = {
    SimpleReader.readLine(s"$prompt\n") getOrElse {
      val badInputMessage = "Unable to read input"
      val updatedPrompt = if (prompt.startsWith(badInputMessage)) prompt else s"$badInputMessage\n$prompt"
      readInput(updatedPrompt)
    }
  }
}
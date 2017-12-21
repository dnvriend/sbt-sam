package com.github.dnvriend.sbt.aws.task

import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.event.{ ProgressEvent, ProgressEventType }

import scalaz.Show

trait AwsProgressListenerOps {
  implicit def ToProgressListenerOps[A <: AmazonWebServiceRequest](that: A): ToProgressListenerOps[A] = new ToProgressListenerOps(that)
}
object AwsProgressListenerOps extends AwsProgressListenerOps

class ToProgressListenerOps[A <: AmazonWebServiceRequest](that: A) {
  def addProgressListener(f: ProgressEvent => Unit): A = {
    that.setGeneralProgressListener((progressEvent: ProgressEvent) => f(progressEvent))
    that
  }

  def addPrintlnEventLogger: A = {
    addProgressListener(AwsProgressListener.printlnEventListener)
  }
  def addProgressBar: A = {
    addProgressListener(AwsProgressListener.genericProgressBar)
  }
  def addDebuggingProgressBar: A = {
    addProgressListener(AwsProgressListener.debuggingProgressBar)
  }
}

object AwsProgressListener {
  def calc(nr: Int, total: Int): Int = {
    (100 / total) * nr
  }
  def genericProgressBar(event: ProgressEvent): Unit = event.getEventType match {
    case ProgressEventType.CLIENT_REQUEST_STARTED_EVENT  => render(ProgressBar(calc(1, 8)))
    case ProgressEventType.HTTP_REQUEST_STARTED_EVENT    => render(ProgressBar(calc(2, 8)))
    case ProgressEventType.HTTP_REQUEST_COMPLETED_EVENT  => render(ProgressBar(calc(3, 8)))
    case ProgressEventType.RESPONSE_CONTENT_LENGTH_EVENT => render(ProgressBar(calc(4, 8)))
    case ProgressEventType.HTTP_RESPONSE_STARTED_EVENT   => render(ProgressBar(calc(5, 8)))
    case ProgressEventType.RESPONSE_BYTE_TRANSFER_EVENT  => render(ProgressBar(calc(6, 8)))
    case ProgressEventType.HTTP_RESPONSE_COMPLETED_EVENT => render(ProgressBar(calc(7, 8)))
    case ProgressEventType.CLIENT_REQUEST_SUCCESS_EVENT  => render(ProgressBar(100))
    case ProgressEventType.CLIENT_REQUEST_FAILED_EVENT   => render(ProgressBar(100))
  }
  def render(pb: ProgressBar)(implicit show: Show[ProgressBar]): Unit = {
    println(show.shows(pb))
  }

  def debuggingProgressBar(event: ProgressEvent): Unit = {
    println(event)
    genericProgressBar(event)
  }

  def printlnEventListener(event: ProgressEvent): Unit = {
    println(event)
  }
}

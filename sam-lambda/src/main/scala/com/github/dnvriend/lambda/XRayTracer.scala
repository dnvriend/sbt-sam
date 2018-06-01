package com.github.dnvriend.lambda

import com.amazonaws.xray.entities.{ Segment, Subsegment }
import com.amazonaws.xray.{ AWSXRayRecorder, AWSXRayRecorderBuilder }

trait XRayTracer {
  private val recorder: AWSXRayRecorder = AWSXRayRecorderBuilder.defaultRecorder()

  def withSubsegment[A](name: String, f: Subsegment => A): A = {
    recorder.createSubsegment[A](name, new java.util.function.Function[Subsegment, A] {
      override def apply(t: Subsegment): A = f(t)
    })
  }

  def withSegment[A](name: String, f: Segment => A): A = {
    recorder.createSegment(name, new java.util.function.Function[Segment, A] {
      override def apply(t: Segment): A = f(t)
    })
  }
}

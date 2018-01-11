package com.github.dnvriend.sam.akka.stream

import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, Materializer }

trait AkkaResources {
  implicit def system: ActorSystem = ActorSystem()
  implicit def mat: Materializer = ActorMaterializer()
}

package com.github.dnvriend.mock

import com.github.dnvriend.service.EventStore
import play.api.libs.json.{ Reads, Writes }

import scalaz._
import scalaz.Scalaz._

object MockEventStore {
  def apply[Event](xs: List[Event]): MockEventStore = {
    new MockEventStore(xs)
  }
}

class MockEventStore(xs: List[_]) extends EventStore {
  override def put[Event: Writes](id: String, sequenceNr: Int, event: Event): Disjunction[Throwable, Unit] = {
    ().right[Throwable]
  }

  override def find[Event: Reads](id: String): Disjunction[Throwable, List[(Int, Event)]] = {
    xs.map(_.asInstanceOf[Event]).zipWithIndex.map({ case (event, id) => (id, event) }).right[Throwable]
  }
}

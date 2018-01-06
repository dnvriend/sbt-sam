package com.github.dnvriend.service

import com.github.dnvriend.mock.MockEventStore
import com.github.dnvriend.test.TestSpec
import play.api.libs.json.{Format, Json}

import scalaz._

object PersonEvent {
  implicit val format: Format[PersonEvent] = Json.format
}
case class PersonEvent(id: String, name: String, newName: Option[String])

case class PersonAggregate(id: String, name: String)

class AggregateTest extends TestSpec {
  it should "replay events from the event store" in {
    val events: List[PersonEvent] = List(
      PersonEvent("1", "foo", None),
      PersonEvent("1", "foo", Option("bar")),
      PersonEvent("1", "foo", Option("baz")),
    )
    val aggregate = Aggregate[PersonEvent, PersonAggregate, Unit]("1", MockEventStore(events)) { event =>
      State {
        case None => event match {
          case PersonEvent(id, name, None) => (Option(PersonAggregate(id, name)), ())
        }
        case person => event match {
          case PersonEvent(_, _, Some(newName)) => (person.map(_.copy(name = newName)), ())
        }
      }
    }

    aggregate.handle {
      case Some(person) => (person, PersonEvent("1", "foo", None))
    }.map(_._1) should beRight(PersonAggregate("1", "baz"))

  }
}

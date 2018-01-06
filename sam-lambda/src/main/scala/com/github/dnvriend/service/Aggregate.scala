package com.github.dnvriend.service

import com.github.dnvriend.lambda.SamContext
import com.github.dnvriend.repo.dynamodb.DynamoDBJsonWithRangeKeyRepository
import play.api.libs.json.{ Format, Reads, Writes }

import scalaz.Scalaz._
import scalaz._

trait EventStore {
  def put[Event: Writes](id: String, sequenceNr: Int, event: Event): Disjunction[Throwable, Unit]
  def find[Event: Reads](id: String): Disjunction[Throwable, List[(Int, Event)]]
}

class DynamoDBEventStore(tableName: String, ctx: SamContext, maxEvents: Int) extends EventStore {
  val eventStore: DynamoDBJsonWithRangeKeyRepository = {
    new DynamoDBJsonWithRangeKeyRepository(tableName, ctx, "aggregate_id", "sequence_nr", "json")
  }
  override def put[Event: Writes](id: String, sequenceNr: Int, event: Event): Disjunction[Throwable, Unit] = {
    eventStore.put(id, sequenceNr.toString, event).map(_ => ())
  }

  override def find[Event: Reads](id: String): Disjunction[Throwable, List[(Int, Event)]] = {
    eventStore.find[Event](id, maxEvents).map(xs => {
      xs
        .map({ case (_, seqNr, event) => (seqNr.toInt, event) })
        .sortBy(_._1)
    })
  }
}

object Aggregate {
  def apply[Event: Format, S, A](
    id: String,
    tableName: String,
    ctx: SamContext,
    maxEvents: Int)(handleEvent: Event => State[Option[S], A]): Aggregate[Event, S, A] = {
    new Aggregate(id, new DynamoDBEventStore(tableName, ctx, maxEvents))(handleEvent)
  }

  def apply[Event: Format, S, A](
    id: String,
    eventStore: EventStore)(handleEvent: Event => State[Option[S], A]): Aggregate[Event, S, A] = {
    new Aggregate(id, eventStore)(handleEvent)
  }
}

/**
 * The aggregate, effectively a State, is computed by sequencing over a list of
 * 'Events', by evaluating the function 'handleEvent', and manipulating the state along the way.
 * 'handleEvent', receives an Event, the current state, and must return a (manipulated) state,
 * and the evaluated value of the function 'handleEvent', expressed as A.
 */
class Aggregate[Event: Format, S, A](id: String, eventStore: EventStore)(handleEvent: Event => State[Option[S], A]) {

  /**
   * Get events from the event store
   */
  val events: List[(Int, Event)] =
    eventStore.find[Event](id).
      valueOr(t => {
        println("Error getting events: " + t.getMessage)
        Nil
      })

  /**
   * Determine the current state
   */
  private val (state, _) = events.map(_._2).traverseS(handleEvent).run(Option.empty[S])
  /**
   * Determine the max sequence Nr
   */
  def maxSeqNr: Int = {
    val xs: List[Int] = events.map(_._1)
    if (xs.isEmpty) 0 else xs.max
  }

  /**
   * Invoke the Aggregate
   */
  def handle(f: Option[S] => (S, Event)): Disjunction[Throwable, (S, Event)] = for {
    result <- Disjunction.fromTryCatchNonFatal(f(state))
    _ <- eventStore.put(id, maxSeqNr + 1, result._2)
  } yield result

  /**
   * Returns the current state of the aggregate
   */
  def currentState: Option[S] = state
}
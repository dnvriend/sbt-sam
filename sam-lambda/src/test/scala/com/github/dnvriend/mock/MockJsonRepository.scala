package com.github.dnvriend.mock

import com.github.dnvriend.repo.JsonRepository
import play.api.libs.json.{ Reads, Writes }

object MockJsonRepository extends JsonRepository {
  /**
   * Stores a value with key 'id'
   */
  override def put[A: Writes](id: String, value: A): Unit = ()

  /**
   * Returns a value, if present with key 'id'
   */
  override def find[A: Reads](id: String): Option[A] = None

  /**
   * Updates a value with key 'id'
   */
  override def update[A: Writes](id: String, value: A): Unit = ()

  /**
   * Deletes a value with key 'id'
   */
  override def delete(id: String): Unit = ()

  /**
   * Returns a list of values, default 100 items
   */
  override def list[A: Reads](limit: Int): List[(String, A)] = List.empty
}

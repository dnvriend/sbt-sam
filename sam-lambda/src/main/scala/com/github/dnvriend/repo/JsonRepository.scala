package com.github.dnvriend.repo

import play.api.libs.json.{ Reads, Writes }

trait JsonRepository {
  /**
   * Stores a value with key 'id'
   */
  def put[A: Writes](id: String, value: A): Unit

  /**
   * Returns a value, if present with key 'id'
   */
  def find[A: Reads](id: String): Option[A]

  /**
   * Updates a value with key 'id'
   */
  def update[A: Writes](id: String, value: A): Unit

  /**
   * Deletes a value with key 'id'
   */
  def delete(id: String): Unit

  /**
   * Returns a list of values, default 100 items
   */
  def list[A: Reads](limit: Int = 100): List[(String, A)]
}

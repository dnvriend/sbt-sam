package com.github.dnvriend.repo

trait BinaryRepository {
  /**
   * Stores a value with key 'id'
   */
  def put(id: String, value: Array[Byte]): Unit

  /**
   * Returns a value, if present with key 'id'
   */
  def find(id: String): Option[Array[Byte]]

  /**
   * Updates a value with key 'id'
   */
  def update(id: String, value: Array[Byte]): Unit

  /**
   * Deletes a value with key 'id'
   */
  def delete(id: String): Unit

  /**
   * Returns a list of values, default 100 items
   */
  def list(limit: Int = 100): List[(String, Array[Byte])]
}

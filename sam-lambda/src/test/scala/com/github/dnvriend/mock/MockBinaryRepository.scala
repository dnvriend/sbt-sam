package com.github.dnvriend.mock

import com.github.dnvriend.repo.BinaryRepository

object MockBinaryRepository extends BinaryRepository {
  /**
   * Stores a value with key 'id'
   */
  override def put(id: String, value: Array[Byte]): Unit = ()

  /**
   * Returns a value, if present with key 'id'
   */
  override def find(id: String): Option[Array[Byte]] = None

  /**
   * Updates a value with key 'id'
   */
  override def update(id: String, value: Array[Byte]): Unit = ()

  /**
   * Deletes a value with key 'id'
   */
  override def delete(id: String): Unit = ()

  /**
   * Returns a list of values, default 100 items
   */
  override def list(limit: Int): List[(String, Array[Byte])] = List.empty
}

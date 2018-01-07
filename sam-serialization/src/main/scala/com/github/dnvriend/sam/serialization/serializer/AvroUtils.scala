package com.github.dnvriend.sam.serialization
package serializer

import java.io.ByteArrayOutputStream

import com.sksamuel.avro4s._
import org.apache.avro.{ Schema, SchemaNormalization }

object AvroUtils {
  /**
   * Generates a SHA-1 fingerprint of the normalized avro schema
   * used for schema/shape identification
   */
  def fingerPrint(schema: Schema): DTry[Array[Byte]] = {
    SchemaNormalization.parsingFingerprint("SHA-256", schema).safe
  }

  /**
   * Encodes data - byte array - as base64 string
   */
  def encodeBase64(data: Array[Byte]): DTry[String] = {
    java.util.Base64.getEncoder.encodeToString(data).safe
  }

  /**
   * Returns the string as data - byte array - encoded as UTF-8
   */
  def encodeUtf8(str: String): DTry[Array[Byte]] = {
    str.getBytes("UTF-8").safe
  }

  /**
   * Decodes UTF-8 encoded data - byte array - to String
   */
  def decodeUtf8(data: Array[Byte]): DTry[String] = {
    new String(data, "UTF-8").safe
  }

  /**
   * Decodes base64 encoded data - byte array
   */
  def decodeBase64(base64: String): DTry[Array[Byte]] = {
    java.util.Base64.getDecoder.decode(base64).safe
  }

  /**
   * Encodes data - byte array - as hex string
   */
  def encodeHex(data: Array[Byte]): DTry[String] = {
    javax.xml.bind.DatatypeConverter.printHexBinary(data).safe
  }

  /**
   * Decodes hex string encoded data - byte array
   */
  def decodeHex(hexString: String): DTry[Array[Byte]] = {
    javax.xml.bind.DatatypeConverter.parseHexBinary(hexString).safe
  }

  /**
   * Compresses data - byte array - using GZIP
   */
  def compress(data: Array[Byte]): DTry[Array[Byte]] = {
    val bos = new java.io.ByteArrayOutputStream(data.length)
    val gzip = new java.util.zip.GZIPOutputStream(bos)
    gzip.write(data)
    gzip.close()
    bos.close()
    bos.toByteArray
  }.safe

  /**
   * Decompresses data - byte array - using GZIP
   */
  def decompress(data: Array[Byte]): DTry[Array[Byte]] = {
    val bis = new java.io.ByteArrayInputStream(data)
    val gzip = new java.util.zip.GZIPInputStream(bis, data.length)
    Stream.continually(gzip.read()).takeWhile(_ != -1).map(_.toByte).toArray
  }.safe

  /**
   * Serializes a value 'A' to AVRO encoded data - byte array
   */
  def serialize[A: ToRecord](data: A)(implicit schemaFor: SchemaFor[A]): DTry[Array[Byte]] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[A](baos)
    output.write(data)
    output.flush
    output.close()
    baos.toByteArray
  }.safe

  /**
   * Deserializes data - byte array - to a value 'A'
   */
  def deserialize[A: FromRecord: SchemaFor](data: Array[Byte], writerSchema: Schema): DTry[A] = {
    val input = new AvroBinaryInputStream[A](new java.io.ByteArrayInputStream(data), Option(writerSchema))
    input.iterator().next()
  }.safe
}
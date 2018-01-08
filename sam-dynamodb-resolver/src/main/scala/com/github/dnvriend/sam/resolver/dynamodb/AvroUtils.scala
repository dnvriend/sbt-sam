package com.github.dnvriend.sam.resolver.dynamodb

import org.apache.avro.{ Schema, SchemaNormalization }

object AvroUtils {
  /**
   * Generates a SHA-1 fingerprint of the normalized avro schema
   * used for schema/shape identification
   */
  def fingerPrint(schema: Schema): Array[Byte] = {
    SchemaNormalization
      .parsingFingerprint("SHA-256", schema)
  }

  /**
   * Encodes data - byte array - as base64 string
   */
  def encodeBase64(data: Array[Byte]): String = {
    java.util.Base64.getEncoder.encodeToString(data)
  }

  /**
   * Returns the string as data - byte array - encoded as UTF-8
   */
  def encodeUtf8(str: String): Array[Byte] = {
    str.getBytes("UTF-8")
  }

  /**
   * Decodes utf-8 encoded data - byte array
   */
  def decodeUtf8(data: Array[Byte]): String = {
    new String(data, "UTF-8")
  }

  /**
   * Decodes base64 encoded data - byte array
   */
  def decodeBase64(base64: String): Array[Byte] = {
    java.util.Base64.getDecoder.decode(base64)
  }

  /**
   * Encodes data - byte array - as hex string
   */
  def encodeHex(data: Array[Byte]): String = {
    javax.xml.bind.DatatypeConverter.printHexBinary(data)
  }

  /**
   * Decodes hex string encoded data - byte array
   */
  def decodeHex(hexString: String): Array[Byte] = {
    javax.xml.bind.DatatypeConverter.parseHexBinary(hexString)
  }

  /**
   * Compresses data - byte array - using GZIP
   */
  def compress(data: Array[Byte]): Array[Byte] = {
    val bos = new java.io.ByteArrayOutputStream(data.length)
    val gzip = new java.util.zip.GZIPOutputStream(bos)
    gzip.write(data)
    gzip.close()
    bos.close()
    bos.toByteArray
  }

  /**
   * Decmpresses data - byte array - using GZIP
   */
  def decompress(data: Array[Byte]): Array[Byte] = {
    val bis = new java.io.ByteArrayInputStream(data)
    val gzip = new java.util.zip.GZIPInputStream(bis, data.length)
    Stream.continually(gzip.read()).takeWhile(_ != -1).map(_.toByte).toArray
  }

  /**
   * Parse avroJsonString to apache avro schema type
   */
  def parseAvro(schema: String): org.apache.avro.Schema = {
    new org.apache.avro.Schema.Parser().parse(schema)
  }
}
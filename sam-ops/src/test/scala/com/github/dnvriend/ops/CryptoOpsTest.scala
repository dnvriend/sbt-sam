package com.github.dnvriend.ops

import com.github.dnvriend.test.TestSpec

import scalaz.@@

class CryptoOpsTest extends TestSpec with CryptoOps {
  val text: String = "Hello World"
  val plainText: Array[Byte] = text.getBytes("UTF-8")
  val plainTextUtf8: Array[Byte] @@ UTF8 = text.getBytes("UTF-8").wrap[UTF8]
  val plainTextAvroJson: Array[Byte] @@ AvroJson = text.getBytes("UTF-8").wrap[AvroJson]
  val encryptKey: String = "Key"
  val encryptSalt: String = "Salt"

  it should "encrypt bytes" in {
    val cipherText: Array[Byte] = plainText.encrypt.symmetric.aes(encryptKey, encryptSalt)
    val clearText: Array[Byte] = cipherText.decrypt.symmetric.aes(encryptKey, encryptSalt)
    clearText shouldBe plainText
  }

  it should "encrypt UTF8 tagged bytes" in {
    val cipherText: Array[Byte] = plainTextUtf8.encrypt.symmetric.aes(encryptKey, encryptSalt)
    val clearText: Array[Byte] = cipherText.decrypt.symmetric.aes(encryptKey, encryptSalt)
    clearText shouldBe plainText
  }

  it should "encrypt AvroJson tagged bytes" in {
    val cipherText: Array[Byte] = plainTextAvroJson.encrypt.symmetric.aes(encryptKey, encryptSalt)
    val clearText: Array[Byte] = cipherText.decrypt.symmetric.aes(encryptKey, encryptSalt)
    clearText shouldBe plainText
  }
}

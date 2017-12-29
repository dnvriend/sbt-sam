package com.github.dnvriend.crypto

import java.io.{ ByteArrayInputStream, InputStream }

import com.github.dnvriend.test.TestSpec
import org.scalatest.Ignore

/**
 * Note, be sure to have JCE - Unlimited Strength Jurisdiction Policy Files - installed.
 * see: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
 * see: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=7044060
 * see: https://bugs.openjdk.java.net/browse/JDK-8072452
 * see: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6521495
 * see: https://stackoverflow.com/questions/6851461/java-why-does-ssl-handshake-give-could-not-generate-dh-keypair-exception
 */
@Ignore
class AwsEncryptionTest extends TestSpec {
  final val CmkArnEuWest1: String = "arn:aws:kms:eu-west-1:015242279314:key/04a8c913-9c2b-42e8-a4b5-1bd2beccc3f2"
  final val CmkArnEuWest2: String = "arn:aws:kms:eu-west-2:015242279314:key/8272df61-67ce-42ec-b3b3-a8f2e080ed4b"
  final val CmkArnEuWest3: String = "arn:aws:kms:eu-west-3:015242279314:key/097e5e4c-e4ac-46f2-86c3-6b391c9dbf83"
  final val CmkArnEuCentral1: String = "arn:aws:kms:eu-central-1:015242279314:key/5a27910f-7997-41c1-8921-76046e4014d4"

  val plaintext: String = "Hello World"
  val plaintextBytes: Array[Byte] = plaintext.getBytes("UTF-8")
  def plaintextInputStream(): InputStream = new ByteArrayInputStream(plaintextBytes)

  it should "encrypt/decrypt text in eu west 1" in {
    val cryptoEurope = AwsEncryption.apply(
      List(
        CmkArnEuWest1,
        CmkArnEuWest2,
        CmkArnEuWest3,
        CmkArnEuCentral1
      )
    )
    val ciphertext = cryptoEurope.encryptBytes(plaintextBytes)
    cryptoEurope.decryptBytes(ciphertext).getResult shouldBe plaintextBytes
  }
}

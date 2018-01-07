package com.github.dnvriend.sam.serialization
package crypto

import java.io.{ InputStream, OutputStream }

import com.amazonaws.encryptionsdk.kms.{ KmsMasterKey, KmsMasterKeyProvider }
import com.amazonaws.encryptionsdk.multi.MultipleProviderFactory
import com.amazonaws.encryptionsdk._

import scala.collection.JavaConverters._

/**
 * Constructor for AwsEncryption
 */
object AwsEncryption {
  /**
   * Provided with a single CMK, data can be encrypted and decrypted.
   * The resulting 'Encrypted Message' a data structure that contains the
   * encrypted data (ciphertext) and all encrypted data keys, can be
   * safely stored on any data carrier.
   */
  def apply(cmkArn: String): AwsEncryption = {
    apply(List(cmkArn))
  }
  /**
   * Provided with one or more Customer Master Keys (CMK) ARNs from eg. one or
   * more regions, data can be encrypted and decrypted. The resulting
   * 'Encrypted Message' a data structure that contains the
   * encrypted data (ciphertext) and all encrypted data keys, can be
   * safely stored on any data carrier.
   */
  def apply(cmkArn: List[String]): AwsEncryption = {
    val providers: List[KmsMasterKeyProvider] = {
      cmkArn.map(cmkArn => new KmsMasterKeyProvider(cmkArn))
    }
    val multiMasterKeyProvider: MasterKeyProvider[KmsMasterKey] = {
      MultipleProviderFactory.buildMultiProvider(providers.asJava)
        .asInstanceOf[MasterKeyProvider[KmsMasterKey]]
    }
    apply(multiMasterKeyProvider)
  }

  /**
   * Provided with an AWS Encryption Crypto and a  MasterKeyProvider[KmsMasterKey],
   * data can be encrypted and decrypted. The resulting 'Encrypted Message' a data structure
   * that contains the encrypted data (ciphertext) and all encrypted data keys, can be
   * safely stored on any data carrier.
   */
  def apply(provider: MasterKeyProvider[KmsMasterKey]): AwsEncryption = {
    val crypto: AwsCrypto = new AwsCrypto()
    new AwsEncryption(crypto, provider)
  }

  /**
   * Use a single master key provider for the given cmk
   */
  def singleProvider(cmkArn: String): AwsEncryption = {
    val crypto: AwsCrypto = new AwsCrypto()
    val provider = new KmsMasterKeyProvider(cmkArn)
    new AwsEncryption(crypto, provider)
  }
}

/**
 * Provided with a MasterKeyProvider[KmsMasterKey], data can be encrypted
 * and decrypted. The resulting 'Encrypted Message' a data structure
 * that contains the encrypted data (ciphertext) and all encrypted data keys, can be
 * safely stored on any data carrier.
 *
 * see: https://docs.aws.amazon.com/encryption-sdk/latest/developer-guide/java.html
 * see: https://docs.aws.amazon.com/encryption-sdk/latest/developer-guide/java-example-code.html
 */
class AwsEncryption(crypto: AwsCrypto, masterKeyProvider: MasterKeyProvider[KmsMasterKey]) {
  /**
   * Returns he 'EncryptedMessage', a data structure that contains the
   * encrypted data (ciphertext) and all encrypted data keys.
   */
  def encryptBytes(plaintext: Array[Byte]): DTry[Array[Byte]] = {
    crypto.encryptData(masterKeyProvider, plaintext).getResult()
  }.safe

  /**
   * Returns `CryptoResult` when given an 'EncryptedMessage', a data structure that contains the
   * encrypted data (ciphertext) and all encrypted data keys. The 'CryptoResult'
   * Represents the result of an operation by AwsCrypto. It not only captures the
   * result of the operation - Array[Byte] -  but also additional metadata such as the
   * encryptionContext, algorithm, MasterKey(s), and any other information
   * captured in the CiphertextHeaders.
   */
  def decryptBytes(encryptedMessage: Array[Byte]): DTry[CryptoResult[Array[Byte], KmsMasterKey]] = {
    crypto.decryptData(masterKeyProvider, encryptedMessage)
  }.safe

  /**
   * Returns the cleartext data
   */
  def decryptBytesResult(encryptedMessage: Array[Byte]): DTry[Array[Byte]] = {
    crypto.decryptData(masterKeyProvider, encryptedMessage)
  }.safe.map(_.getResult)

  /**
   * Returns `CryptoResult` when given a plaintext. The 'CryptoResult'
   * Represents the result of an operation by AwsCrypto. It not only captures the
   * result of the operation - String -  but also additional metadata such as the
   * encryptionContext, algorithm, MasterKey(s), and any other information
   * captured in the CiphertextHeaders.
   * The result is a base64 encoded String - the encrypted message that can safely
   */
  def encryptString(plaintext: String): CryptoResult[String, KmsMasterKey] = {
    crypto.encryptString(masterKeyProvider, plaintext)
  }

  /**
   * Returns `CryptoResult` when given an 'EncryptedMessage', a data structure that contains the
   * encrypted data (ciphertext) and all encrypted data keys. The 'CryptoResult'
   * Represents the result of an operation by AwsCrypto. It not only captures the
   * result of the operation - String -  but also additional metadata such as the
   * encryptionContext, algorithm, MasterKey(s), and any other information
   * captured in the CiphertextHeaders.
   */
  def decryptString(encryptedMessage: String): CryptoResult[String, KmsMasterKey] = {
    crypto.decryptString(masterKeyProvider, encryptedMessage)
  }

  /**
   * Returns a CryptoInputStream which encrypts the data after reading it from the underlying InputStream.
   */
  def createEncryptingStream(is: InputStream): CryptoInputStream[KmsMasterKey] = {
    crypto.createEncryptingStream(masterKeyProvider, is)
  }

  /**
   * Returns a CryptoOutputStream which decrypts the data prior to passing it onto the underlying OutputStream.
   */
  def createDecryptingStream(os: OutputStream): CryptoOutputStream[KmsMasterKey] = {
    crypto.createDecryptingStream(masterKeyProvider, os)
  }
}
package com.github.dnvriend.crypto

import java.io.{ InputStream, OutputStream }

import com.amazonaws.encryptionsdk._
import com.amazonaws.encryptionsdk.kms.{ KmsMasterKey, KmsMasterKeyProvider }
import com.amazonaws.encryptionsdk.multi.MultipleProviderFactory

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
}

/**
 * Provided with a MasterKeyProvider[KmsMasterKey], data can be encrypted
 * and decrypted. The resulting 'Encrypted Message' a data structure
 * that contains the encrypted data (ciphertext) and all encrypted data keys, can be
 * safely stored on any data carrier.
 */
class AwsEncryption(crypto: AwsCrypto, masterKeyProvider: MasterKeyProvider[KmsMasterKey]) {
  /**
   * Returns he 'EncryptedMessage', a data structure that contains the
   * encrypted data (ciphertext) and all encrypted data keys.
   */
  def encryptBytes(plaintext: Array[Byte]): Array[Byte] = {
    crypto.encryptData(masterKeyProvider, plaintext).getResult()
  }

  /**
   * Returns `CryptoResult` when given an 'EncryptedMessage', a data structure that contains the
   * encrypted data (ciphertext) and all encrypted data keys. The 'CryptoResult'
   * Represents the result of an operation by AwsCrypto. It not only captures the
   * result of the operation - Array[Byte] -  but also additional metadata such as the
   * encryptionContext, algorithm, MasterKey(s), and any other information
   * captured in the CiphertextHeaders.
   */
  def decryptBytes(encryptedMessage: Array[Byte]): CryptoResult[Array[Byte], KmsMasterKey] = {
    crypto.decryptData(masterKeyProvider, encryptedMessage)
  }

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
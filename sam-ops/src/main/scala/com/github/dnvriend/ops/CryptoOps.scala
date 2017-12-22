// Copyright 2017 Dennis Vriend
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.dnvriend.ops

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import java.util

import scalaz.{ @@, Show }

object Crypto {
  def keyToSpec(key: String, salt: String): SecretKeySpec = {
    var keyBytes: Array[Byte] = (salt + key).getBytes("UTF-8")
    val sha: MessageDigest = MessageDigest.getInstance("SHA-256")
    keyBytes = sha.digest(keyBytes)
    keyBytes = util.Arrays.copyOf(keyBytes, 16)
    new SecretKeySpec(keyBytes, "AES")
  }

  def encrypt(javaCipher: JavaCipher, keySpec: SecretKeySpec, plainText: Array[Byte]): Array[Byte] = {
    val cipher: Cipher = Cipher.getInstance(Show[JavaCipher].shows(javaCipher))
    cipher.init(Cipher.ENCRYPT_MODE, keySpec)
    cipher.doFinal(plainText)
  }

  def decrypt(javaCipher: JavaCipher, keySpec: SecretKeySpec, cipherText: Array[Byte]): Array[Byte] = {
    val cipher: Cipher = Cipher.getInstance(Show[JavaCipher].shows(javaCipher))
    cipher.init(Cipher.DECRYPT_MODE, keySpec)
    cipher.doFinal(cipherText)
  }
}

object CryptoOps extends CryptoOps

trait CryptoOps extends ByteArrayOps with AnyOps {
  implicit def ToEncryptionOps(that: Array[Byte]): ToCryptoOps = new ToCryptoOps(that)
  implicit def ToUtf8EncryptionOps(that: Array[Byte] @@ UTF8): ToCryptoOps = new ToCryptoOps(that.unwrap)
  implicit def ToAvroJsonEncryptionOps(that: Array[Byte] @@ AvroJson): ToCryptoOps = new ToCryptoOps(that.unwrap)
}

class ToCryptoOps(that: Array[Byte]) {
  object encrypt {
    object symmetric {
      def aes(key: String, salt: String): Array[Byte] = {
        val keySpec = Crypto.keyToSpec(key, salt)
        Crypto.encrypt(AES_ECB_PKCS5_Padding_128, keySpec, that)
      }
    }
  }

  object decrypt {
    object symmetric {
      def aes(key: String, salt: String): Array[Byte] = {
        val keySpec = Crypto.keyToSpec(key, salt)
        Crypto.decrypt(AES_ECB_PKCS5_Padding_128, keySpec, that)
      }
    }
  }
}

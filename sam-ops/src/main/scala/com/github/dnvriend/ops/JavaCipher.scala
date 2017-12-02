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

import scalaz.Show

/**
 * see: https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html
 */
object JavaCipher {
  implicit val transformation: Show[JavaCipher] = Show.shows(cipher => {
    def getTransformation(algorithm: String, mode: String, padding: String, keySize: Int): String = {
      s"$algorithm/$mode/$padding"
    }
    cipher match {
      case AES_ECB_PKCS5_Padding_128 => getTransformation("AES", "ECB", "PKCS5Padding", 128)
    }
  })

}
sealed trait JavaCipher
case object AES_ECB_PKCS5_Padding_128 extends JavaCipher
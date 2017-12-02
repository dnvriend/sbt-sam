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

import java.io._
import java.nio.charset.Charset

import sbt.io._

import scalaz.Disjunction

object FileOps extends FileOps

trait FileOps {
  implicit def ToFileOps(that: File): ToFileOps = new ToFileOps(that)
  implicit def ToFileStringOps(that: String): ToFileStringOps = new ToFileStringOps(that)
}

class ToFileOps(that: File) extends FunctionalOps {
  def mkdir: Disjunction[Throwable, File] = {
    IO.createDirectory(that)
    that
  }.safe

  def read(charset: Charset = sbt.io.IO.defaultCharset): Disjunction[Throwable, String] = {
    IO.read(that, charset)
  }.safe

  def readBytes: Disjunction[Throwable, Array[Byte]] = {
    IO.readBytes(that)
  }.safe

  def delete: Disjunction[Throwable, File] = {
    IO.delete(that)
    that
  }.safe

  def unzip(toDir: File): Disjunction[Throwable, File] = {
    IO.unzip(that, toDir)
    that
  }.safe

  def zip(sources: Traversable[(File, String)]): Disjunction[Throwable, File] = {
    IO.zip(sources, that)
    that
  }.safe

  def write(str: String, charset: Charset = sbt.io.IO.defaultCharset): Disjunction[Throwable, File] = {
    IO.write(that, str, charset)
    that
  }.safe

  def writeBytes(bytes: Array[Byte]): Disjunction[Throwable, File] = {
    IO.write(that, bytes)
    that
  }.safe
}

class ToFileStringOps(that: String) {
  def file: File = new File(that)
}
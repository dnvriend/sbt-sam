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

import java.io.{ OutputStream, PrintWriter, StringWriter }

import play.api.libs.json.{ Json, Writes }

object OutputStreamOps extends OutputStreamOps

trait OutputStreamOps {
  implicit def ConvertToOSOps(that: OutputStream): ToOutputStreamOps = new ToOutputStreamOps(that)

  def withStringWriter(f: StringWriter => Unit): String = {
    val sw = new StringWriter()
    try f(sw) finally { sw.flush; sw.close() }
    sw.toString
  }

  def withPrintWriter(f: PrintWriter => Unit): String = withStringWriter { sw =>
    val pw = new PrintWriter(sw)
    try f(pw) finally { pw.flush(); pw.close() }
  }
}

class ToOutputStreamOps(that: OutputStream) {
  def write(str: String): OutputStream = {
    that.write(str.getBytes("UTF-8"))
    that
  }
  def write[A: Writes](a: A): OutputStream = {
    write(Json.toJson(a).toString())
  }
  def close: OutputStream = {
    that.close()
    that
  }
}
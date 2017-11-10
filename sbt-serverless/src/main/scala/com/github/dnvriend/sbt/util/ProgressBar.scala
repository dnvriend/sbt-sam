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

package com.github.dnvriend.sbt.util

import sbt.Logger

object ProgressBar {
  private def progressBar(percent: Int): String = {
    val b = "=================================================="
    val s = "                                                  "
    val p = percent / 2
    val z: StringBuilder = new StringBuilder(80)
    z.append("\r[")
    z.append(b.substring(0, p))
    if (p < 50) {
      z.append("=>");
      z.append(s.substring(p))
    }
    z.append("]   ")
    if (p < 5) z.append(" ")
    if (p < 50) z.append(" ")
    z.append(percent)
    z.append("%   ")
    z.mkString
  }

  def show(percent: Int)(implicit log: Logger): Unit = {
    log.info(progressBar(Math.min(100, percent)))
  }
}
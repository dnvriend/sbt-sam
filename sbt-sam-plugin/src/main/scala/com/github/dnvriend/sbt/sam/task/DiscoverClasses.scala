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

package com.github.dnvriend.sbt.sam.task

import sbt.File
import sbt.internal.inc.Analysis
import xsbti.compile.CompileAnalysis

object DiscoverClasses {
  def run(analysis: CompileAnalysis): Set[File] = analysis match {
    case analysis: Analysis =>
      analysis.stamps.allProducts.toSet
  }
}

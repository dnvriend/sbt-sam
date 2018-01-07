package com.github.dnvriend.sam.schema.plugin.service

import sbt._

object DiscoverClassesService {
  def run(analysis: xsbti.compile.CompileAnalysis): Set[File] = {
    analysis match {
      case analysis: sbt.internal.inc.Analysis =>
        analysis.stamps.allProducts.toSet
    }
  }
}

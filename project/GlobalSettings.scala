import sbt._
import sbt.Keys.{publishArtifact, _}

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import de.heikoseeberger.sbtheader.HeaderPlugin

object GlobalSettings extends AutoPlugin with LibraryDependenciesKeys {
  override def trigger = allRequirements
  override def requires = plugins.JvmPlugin && SbtScalariform && HeaderPlugin && LibraryDependencies

  override def globalSettings = Seq(
    scalaVersion := "2.12.4",
    organization := "com.github.dnvriend",
    description := "A plugin for creating enterprise cloud application leveraging serverless compute and managed resources",
    startYear := Some(2017),
    addCompilerPlugin(libScalaMacros),
  ) ++ headerSettings ++ scalariFormSettings ++ resolverSettings ++ compilerSettings ++ publishSourcesAndDocsSettings

  lazy val scalariFormSettings = Seq(
    SbtScalariform.autoImport.scalariformPreferences := {
      SbtScalariform.autoImport.scalariformPreferences.value
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
        .setPreference(DoubleIndentConstructorArguments, true)
        .setPreference(DanglingCloseParenthesis, Preserve)
    }
  )

  lazy val headerSettings = Seq(
    organizationName := "Dennis Vriend",
    startYear := Some(2018),
    licenses := Seq(("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))),
  )

  lazy val resolverSettings = Seq(
    resolvers += Resolver.sonatypeRepo("public"),
    resolvers += Resolver.jcenterRepo,
  )

  lazy val compilerSettings = Seq(
    scalacOptions += "-Ypartial-unification",
    scalacOptions += "-Ydelambdafy:inline",
    scalacOptions += "-unchecked",
    scalacOptions += "-deprecation",
    scalacOptions += "-language:higherKinds",
    scalacOptions += "-language:implicitConversions",
    scalacOptions += "-feature",
  )

  lazy val publishSourcesAndDocsSettings = Seq(
    // disable creating javadoc and scaladoc //
    sources in (Compile,doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false
  )
}

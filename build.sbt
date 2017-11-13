
lazy val sbtSamRoot = (project in file("."))
  .aggregate(ops, sbtSam, samLambda)

lazy val sbtSam = (project in file("sbt-sam"))
  .dependsOn(ops)
  .settings(commonSettings)
  .enablePlugins(AutomateHeaderPlugin, SbtScalariform)

lazy val samLambda = (project in file("sam-lambda"))
  .dependsOn(ops)
  .settings(commonSettings)
  .enablePlugins(AutomateHeaderPlugin, SbtScalariform)

lazy val ops = RootProject(uri("git://github.com/dnvriend/dnvriend-ops.git"))

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

lazy val commonSettings = Seq(
  resolvers += Resolver.sonatypeRepo("public"),
  scalacOptions += "-Ypartial-unification",
  scalacOptions += "-Ydelambdafy:inline",
  scalacOptions += "-unchecked",
  scalacOptions += "-deprecation",
  scalacOptions += "-language:higherKinds",
  scalacOptions += "-language:implicitConversions",
  scalacOptions += "-feature",
  // enable scala code formatting //

  // Scalariform settings
  SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(DanglingCloseParenthesis, Preserve),

  // enable updating file headers //
  organizationName := "Dennis Vriend",
  startYear := Some(2017),
  licenses := Seq(("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))),
  headerMappings := headerMappings.value + (HeaderFileType.scala -> HeaderCommentStyle.CppStyleLineComment),
)
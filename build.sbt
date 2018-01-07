
lazy val `sbt-sam` = (project in file("."))
  .settings(
    publishArtifact := false,
    publish := {},
  ).aggregate(
  `sbt-sam-plugin`,
  `sbt-aws-plugin`,
  `sam-annotations`,
  `sam-lambda`,
  `sam-ops`,
  `sam-schema-plugin`,
  `sam-serialization`,
)

lazy val `sbt-sam-plugin` = (project in file("sbt-sam-plugin"))
  .dependsOn(`sam-ops`, `sbt-aws-plugin`, `sam-testing-lib` % "test->test")
  .enablePlugins(SbtSamPluginSettings, BuildInfoPlugin)

lazy val `sbt-aws-plugin` = (project in file("sbt-aws-plugin"))
  .dependsOn(`sam-ops`, `sam-testing-lib` % "test->test")
  .enablePlugins(SbtAwsPluginSettings)

lazy val `sam-schema-plugin` = (project in file("sam-schema-plugin"))
  .dependsOn(`sam-ops`, `sbt-aws-plugin`, `sam-testing-lib` % "test->test")
  .enablePlugins(SbtSamSchemaPluginSettings)

lazy val `sam-annotations` = (project in file("sam-annotations"))
  .dependsOn(`sam-testing-lib` % "test->test")
  .enablePlugins(SamAnnotationsSettings)

lazy val `sam-lambda` = (project in file("sam-lambda"))
  .dependsOn(`sam-ops`, `sam-testing-lib` % "test->test")
  .enablePlugins(SamLambdaSettings)

lazy val `sam-macros` = (project in file("sam-macros"))
  .dependsOn(`sam-testing-lib` % "test->test")
  .enablePlugins(SamMacrosSettings)

lazy val `sam-ops` = (project in file("sam-ops"))
  .dependsOn(`sam-testing-lib` % "test->test")
  .enablePlugins(SamOpsSettings)

lazy val `sam-serialization` =
  (project in file("sam-serialization"))
    .dependsOn(`sam-testing-lib` % "test->test")
    .enablePlugins(SamSerializationSettings)

lazy val `sam-testing-lib` = (project in file("sam-testing-lib"))
  .enablePlugins(SamTestingLibSettings)
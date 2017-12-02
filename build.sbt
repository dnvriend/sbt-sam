
lazy val `sbt-sam` = (project in file("."))
  .settings(
    publishArtifact := false,
    publish := {},
  ).aggregate(
  `sbt-sam-plugin`,
  `sbt-aws-plugin`,
  `sam-annotations`,
  `sam-lambda`,
  `sam-macros`,
  `sam-ops`,
)

lazy val `sbt-sam-plugin` = (project in file("sbt-sam-plugin"))
  .dependsOn(`sam-ops`, `sbt-aws-plugin`)
  .enablePlugins(SbtSamPluginSettings)

lazy val `sbt-aws-plugin` = (project in file("sbt-aws-plugin"))
  .dependsOn(`sam-ops`)
  .enablePlugins(SbtAwsPluginSettings)

lazy val `sam-annotations` = (project in file("sam-annotations"))
  .enablePlugins(SamAnnotationsSettings)

lazy val `sam-lambda` = (project in file("sam-lambda"))
  .dependsOn(`sam-ops`)
  .enablePlugins(SamLambdaSettings)

lazy val `sam-macros` = (project in file("sam-macros"))
  .enablePlugins(SamMacrosSettings)

lazy val `sam-ops` = (project in file("sam-ops"))
  .enablePlugins(SamOpsSettings)


lazy val sbtSam = (project in file("."))
  .aggregate(ops, sbtSamPlugin, samLambda, samAnnotations)

lazy val sbtSamPlugin = (project in file("sbt-sam"))
  .dependsOn(ops, sbtAwsPlugin)
  .enablePlugins(SbtSamPluginSettings)

lazy val sbtAwsPlugin = (project in file("sbt-aws"))
  .dependsOn(ops)
  .enablePlugins(SbtAwsPluginSettings)

lazy val samAnnotations = (project in file("sam-annotations"))
  .enablePlugins(SamAnnotationsSettings)

lazy val samLambda = (project in file("sam-lambda"))
  .dependsOn(ops)
  .enablePlugins(SamLambdaSettings)

lazy val ops = RootProject(uri("git://github.com/dnvriend/dnvriend-ops.git"))

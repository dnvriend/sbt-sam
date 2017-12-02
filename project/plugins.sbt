// https://github.com/sbt/sbt-scalariform
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")

// https://github.com/sbt/sbt-header
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "4.0.0")

// https://github.com/sbt/sbt-release
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")

// https://github.com/sbt/sbt-bintray
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
// enable publishing to jcenter
homepage := Some(url("https://github.com/dnvriend/sbt-sam"))

pomIncludeRepository := (_ => false)

pomExtra := <scm>
  <url>https://github.com/dnvriend/sbt-sam</url>
  <connection>scm:git@github.com:dnvriend/sbt-sam.git</connection>
  </scm>
  <developers>
    <developer>
      <id>dnvriend</id>
      <name>Dennis Vriend</name>
      <url>https://github.com/dnvriend</url>
    </developer>
  </developers>

publishMavenStyle := true

bintrayPackageLabels := Seq("serverless application model", "sam", "aws", "lambda", "sbt", "scala")

bintrayPackageAttributes ~=
  (_ ++ Map(
    "website_url" -> Seq(bintry.Attr.String("https://github.com/dnvriend/sbt-sam")),
    "github_repo" -> Seq(bintry.Attr.String("https://github.com/dnvriend/sbt-sam.git")),
    "issue_tracker_url" -> Seq(bintry.Attr.String("https://github.com/dnvriend/sbt-sam.git/issues/"))
  )
)

//bintrayRepository := "maven"

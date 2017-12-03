package com.github.dnvriend.sbt.sam.task

final case class ProjectConfiguration()

object GetProjectConfiguration {
  // s3 dir structure:
  // person-repository-dev-sbtsamdeploymentbucket-hex-md5
  //   |-- sbtsam/person-repository/dev/longmillis-2017-10-23T15:22:16.044Z/compiled-cloudformation-template.json
  //   |-- sbtsam/person-repository/dev/longmillis-2017-10-23T15:22:16.044Z/person-repository.zip

  // lambda name:
  // person-repository-dev-post-person

  // cloudformation:
  // person-repository-dev
}

package com.github.dnvriend.sbt.sam.cf.resource.iam.policy

sealed trait CFIamServicePrincipal { def name: String }
object CFIamServicePrincipal {
  case object AmazonFirehoseServicePrincipal extends CFIamServicePrincipal { val name = "firehose.amazonaws.com" }
}

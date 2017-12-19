package com.github.dnvriend.sbt.aws.domain

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.identitymanagement.model.User
import sbt._

import scalaz.Disjunction

object IAMDomain {
  object ProfileLocation {
    def fromFile(value: File): ProfileLocation = {
      ProfileLocation(value)
    }
  }
  final case class ProfileLocation(value: File)

  object AwsCredentials {
    def fromAWSCredentials(value: AWSCredentials): AwsCredentials = {
      AwsCredentials(value)
    }
  }
  final case class AwsCredentials(value: AWSCredentials)

  object AwsRegion {
    def fromRegions(value: Regions): AwsRegion = {
      AwsRegion(value)
    }
    def fromString(value: String): Disjunction[Throwable, AwsRegion] = {
      Disjunction.fromTryCatchNonFatal(AwsRegion(Regions.fromName(value)))
    }
  }
  final case class AwsRegion(value: Regions)
  final case class CredentialsRegionAndUser(credentialsAndRegion: CredentialsProfileAndRegion, user: User)
  final case class CredentialsProfileAndRegion(credentials: AwsCredentials, profileLocation: ProfileLocation, region: Regions)
}

package com.github.dnvriend.sbt.aws.task

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions

import scalaz.{@@, Show, Tag}
import scalaz.std.option._
import scalaz.syntax.all._

object AwsCredentialsProvider {
  def instance[A <: AwsCredentials](f: A => AWSCredentialsProvider): AwsCredentialsProvider[A] = {
    (a: A) => f(a)
  }
}

trait AwsCredentialsProvider[A <: AwsCredentials] {
  def create(a: A): AWSCredentialsProvider
}

object AwsCredentials {
  implicit val awsCredentialsProvider: AwsCredentialsProvider[AwsCredentials] =
    AwsCredentialsProvider.instance {
      case AwsCredentialsProfile(profile) => new ProfileCredentialsProvider(profile)
      case AwsCredentialsInformation(accessKey, secretKey) =>
        new AWSStaticCredentialsProvider(
          new BasicAWSCredentials(Tag.unwrap(accessKey), Tag.unwrap(secretKey))
        )
    }
}

// credentials ADT
sealed trait AwsCredentials

final case class AwsCredentialsProfile(profile: String) extends AwsCredentials

final case class AwsCredentialsInformation(accessKeyId: String @@ AwsAccessKeyId, awsSecretKey: String @@ AwsSecretAccessKey) extends AwsCredentials

object CredentialsAndRegion {
  implicit val show: Show[CredentialsAndRegion] = Show.shows(model => {
    import model._
    s"""
      |=============================================
      |Credentials And Region:
      |=============================================
      |Region: $region
      |AWSAccessKeyId: ${credentialsProvider.getCredentials.getAWSAccessKeyId}
      |AWSSecretKey: ${credentialsProvider.getCredentials.getAWSSecretKey}
    """.stripMargin
  })
}

final case class CredentialsAndRegion(credentialsProvider: AWSCredentialsProvider, region: Regions) {
  override def toString: String = CredentialsAndRegion.show.shows(this)
}

// tags //
sealed trait AwsAccessKeyId
sealed trait AwsSecretAccessKey
sealed trait AwsClientId

object GetCredentialsProvider {
  def awsCredentialsInformation(awsAccessKeyId: Option[String @@ AwsAccessKeyId],
                                awsSecretAccessKey: Option[String @@ AwsSecretAccessKey],
                               ): Option[AwsCredentials] = {
    (awsAccessKeyId |@| awsSecretAccessKey) (AwsCredentialsInformation.apply)
  }

  def awsProfileInformation(profile: Option[String]): Option[AwsCredentials] = {
    profile.map(AwsCredentialsProfile.apply)
  }

  def getCredentials(awsAccessKeyId: Option[String @@ AwsAccessKeyId],
                     awsSecretAccessKey: Option[String @@ AwsSecretAccessKey],
                     profile: Option[String],
                    )(implicit provider: AwsCredentialsProvider[AwsCredentials]): AWSCredentialsProvider = {
    val awsCredentials: AwsCredentials = {
      awsCredentialsInformation(awsAccessKeyId, awsSecretAccessKey)
        .orElse(awsProfileInformation(profile))
        .getOrElse(throw new IllegalStateException("No AWS credentials"))
    }
    provider.create(awsCredentials)
  }

  def getCredentialsAndRegion(awsAccessKeyId: Option[String @@ AwsAccessKeyId],
                              awsSecretAccessKey: Option[String @@ AwsSecretAccessKey],
                              profile: Option[String],
                              region: Option[String]): CredentialsAndRegion = {
    region.map { region =>
      CredentialsAndRegion(
        getCredentials(awsAccessKeyId, awsSecretAccessKey, profile),
        Regions.fromName(region)
      )
    }.getOrElse(throw new IllegalStateException("No AWS region"))
  }
}

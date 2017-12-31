package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.cognitoidp.model._
import com.amazonaws.services.cognitoidp.{ AWSCognitoIdentityProvider, AWSCognitoIdentityProviderClientBuilder }

import scala.collection.JavaConverters._
import scala.util.Try

object CognitoIdpOperations {
  def client(): AWSCognitoIdentityProvider = {
    AWSCognitoIdentityProviderClientBuilder.defaultClient()
  }

  def listUserPools(client: AWSCognitoIdentityProvider): List[UserPoolDescriptionType] = {
    Try(client.listUserPools(new ListUserPoolsRequest().withMaxResults(25)).getUserPools.asScala.toList).getOrElse(Nil)
  }

  def findUserPool(userPoolName: String, client: AWSCognitoIdentityProvider): Option[UserPoolDescriptionType] = {
    listUserPools(client).find(_.getName == userPoolName)
  }

  def describeUserPool(userPoolId: String, client: AWSCognitoIdentityProvider): Option[DescribeUserPoolResult] = {
    Try(client.describeUserPool(new DescribeUserPoolRequest()
      .withUserPoolId(userPoolId)
    )).toOption
  }
}

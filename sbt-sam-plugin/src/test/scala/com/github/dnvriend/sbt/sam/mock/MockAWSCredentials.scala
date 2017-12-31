package com.github.dnvriend.sbt.sam.mock

import com.amazonaws.auth.AWSCredentials

class MockAWSCredentials(awsAccessKeyId: String, awsSecretKey: String) extends AWSCredentials {
  override def getAWSAccessKeyId: String = awsAccessKeyId
  override def getAWSSecretKey: String = awsSecretKey

  override def toString: String = {
    s"MockAWSCredentials($awsAccessKeyId,$awsSecretKey)"
  }
}

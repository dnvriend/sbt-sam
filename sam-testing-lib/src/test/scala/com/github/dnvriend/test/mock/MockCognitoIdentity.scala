package com.github.dnvriend.test.mock

import com.amazonaws.services.lambda.runtime.CognitoIdentity

object MockCognitoIdentity {
  def apply(): CognitoIdentity = new MockCognitoIdentity()
}
class MockCognitoIdentity extends CognitoIdentity {
  override def getIdentityId: String = "mock"

  override def getIdentityPoolId: String = "mock"
}

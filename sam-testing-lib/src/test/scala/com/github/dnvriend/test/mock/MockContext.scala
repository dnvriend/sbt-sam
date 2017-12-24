package com.github.dnvriend.test.mock

import com.amazonaws.services.lambda.runtime.{ ClientContext, CognitoIdentity, Context, LambdaLogger }

object MockContext {
  def apply(): Context = new MockContext()
}

class MockContext extends Context {
  override def getFunctionName: String = "mock"

  override def getRemainingTimeInMillis: Int = 0

  override def getLogger: LambdaLogger = MockLambdaLogger.apply()

  override def getFunctionVersion: String = "mock"

  override def getMemoryLimitInMB: Int = 1024

  override def getClientContext: ClientContext = MockClientContext.apply()

  override def getLogStreamName: String = "mock"

  override def getInvokedFunctionArn: String = "mock"

  override def getIdentity: CognitoIdentity = MockCognitoIdentity.apply()

  override def getLogGroupName: String = "mock"

  override def getAwsRequestId: String = "mock"
}
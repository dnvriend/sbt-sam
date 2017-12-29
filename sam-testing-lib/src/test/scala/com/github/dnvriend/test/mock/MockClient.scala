package com.github.dnvriend.test.mock

import com.amazonaws.services.lambda.runtime.Client

object MockClient {
  def apply(): Client = new MockClient()
}

class MockClient extends Client {
  override def getAppPackageName: String = "mock"

  override def getInstallationId: String = "mock"

  override def getAppTitle: String = "mock"

  override def getAppVersionCode: String = "mock"

  override def getAppVersionName: String = "mock"
}

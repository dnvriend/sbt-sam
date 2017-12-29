package com.github.dnvriend.test.mock

import java.util
import scala.collection.JavaConverters._

import com.amazonaws.services.lambda.runtime.{ Client, ClientContext }

object MockClientContext {
  def apply(): ClientContext = new MockClientContext()
}

class MockClientContext extends ClientContext {
  override def getEnvironment: util.Map[String, String] = Map.empty[String, String].asJava

  override def getCustom: util.Map[String, String] = Map.empty[String, String].asJava

  override def getClient: Client = MockClient.apply
}

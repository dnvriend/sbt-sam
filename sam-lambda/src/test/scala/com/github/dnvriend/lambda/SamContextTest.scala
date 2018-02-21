package com.github.dnvriend.lambda

import com.github.dnvriend.test.TestSpec
import com.github.dnvriend.test.mock.{ MockContext, MockLambdaLogger }

class SamContextTest extends TestSpec {
  it should "resolve to resource naming convention" in {
    val ctx = SamContext(MockContext(), MockLambdaLogger(), "project-name", "stage", "region", "account-id")
    ctx.determineName("resource") shouldBe "stage-project-name-resource"
  }

  it should "resolve to import resource naming convention" in {
    val ctx = SamContext(MockContext(), MockLambdaLogger(), "project-name", "stage", "region", "account-id")
    ctx.determineName("import:component:resource") shouldBe "stage-component-resource"
  }

  it should "resolve to table name for resource naming convention" in {
    val ctx = SamContext(MockContext(), MockLambdaLogger(), "project-name", "stage", "region", "account-id")
    ctx.dynamoDbTableName("table-name") shouldBe "stage-project-name-table-name"
  }

  it should "resolve to table name for import resource naming convention" in {
    val ctx = SamContext(MockContext(), MockLambdaLogger(), "project-name", "stage", "region", "account-id")
    ctx.dynamoDbTableName("import:component:table-name") shouldBe "stage-component-table-name"
  }

  it should "resolve to sns topic for resource naming convention" in {
    val ctx = SamContext(MockContext(), MockLambdaLogger(), "project-name", "stage", "region", "account-id")
    ctx.dynamoDbTableName("sns-name") shouldBe "stage-project-name-sns-name"
  }

  it should "resolve to sns topic for import resource naming convention" in {
    val ctx = SamContext(MockContext(), MockLambdaLogger(), "project-name", "stage", "region", "account-id")
    ctx.dynamoDbTableName("import:component:sns-name") shouldBe "stage-component-sns-name"
  }

  it should "resolve to kinesis name for resource naming convention" in {
    val ctx = SamContext(MockContext(), MockLambdaLogger(), "project-name", "stage", "region", "account-id")
    ctx.kinesisStreamName("kinesis-name") shouldBe "stage-project-name-kinesis-name"
  }

  it should "resolve to kinesis name for import resource naming convention" in {
    val ctx = SamContext(MockContext(), MockLambdaLogger(), "project-name", "stage", "region", "account-id")
    ctx.kinesisStreamName("import:component:kinesis-name") shouldBe "stage-component-kinesis-name"
  }
}

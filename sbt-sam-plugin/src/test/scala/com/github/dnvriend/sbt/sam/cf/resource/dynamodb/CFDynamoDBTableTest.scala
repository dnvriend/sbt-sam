package com.github.dnvriend.sbt.sam.cf.resource.dynamodb

import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.sbt.sam.generators.Generators
import com.github.dnvriend.test.TestSpec
import play.api.libs.json.{ JsValue, Json }

class CFDynamoDBTableTest extends TestSpec with Generators with AllOps {
  it should "generate a full dynamodb table json" in {
    val table: CFDynamoDBTable = iterCFTable.next
    val json = Json.toJson(table)
    val str = Json.prettyPrint(json)
    (json \ "DynamoDBTable").toOption shouldBe 'defined
    (json \ "DynamoDBTable" \ "Type").as[String] shouldBe "AWS::DynamoDB::Table"
    (json \ "DynamoDBTable" \ "Properties").toOption shouldBe 'defined
    val props = (json \ "DynamoDBTable" \ "Properties").as[JsValue]
    (props \ "TableName").toOption shouldBe 'defined
    (props \ "KeySchema").toOption shouldBe 'defined
    (props \ "AttributeDefinitions").toOption shouldBe 'defined
    (props \ "ProvisionedThroughput").toOption shouldBe 'defined
    (props \ "StreamSpecification").toOption shouldBe 'defined
    (props \ "GlobalSecondaryIndexes").toOption shouldBe 'defined
  }
}

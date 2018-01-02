package com.github.dnvriend.sbt.sam.cf.resource.firehose.s3

import com.github.dnvriend.sbt.sam.cf.resource.iam.role.{CFIamRole, CFS3IamPolicy}
import play.api.libs.json.{JsValue, Json, Writes}


/**
  * You are required to have an IAM role when creating a delivery stream. Kinesis Data Firehose assumes that IAM role
  * and gains access to the specified bucket, key, and CloudWatch log group and streams.
  */
object CFS3FirehoseIamRole {
  def createRole(
                  logicalName: String,
                  roleName: String,
                  path: String,
                  managedPolicyArns: List[String],
                ): CFIamRole = {

    val assumeRolePolicyDocument: JsValue = Json.obj()

    val policies: List[CFS3IamPolicy] = List.empty

    CFIamRole(
      logicalName,
      roleName,
      path,
      managedPolicyArns,
      policies,
      assumeRolePolicyDocument
    )
  }
}

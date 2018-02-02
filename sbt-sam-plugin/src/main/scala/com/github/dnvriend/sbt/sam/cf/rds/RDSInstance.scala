package com.github.dnvriend.sbt.sam.cf.rds

import com.github.dnvriend.sbt.sam.cf.resource.Resource
import play.api.libs.json.{JsObject, Json, Writes}

object RDSInstance {
  implicit val writes: Writes[RDSInstance] = Writes.apply(model => {
    import model._
    Json.obj(
      configName -> Json.obj(
        "Type" -> "AWS::RDS::DBInstance",
        "Properties" -> Json.obj(
          "AllocatedStorage" -> allocatedStorage,
          "DBInstanceIdentifier" -> dbInstanceIdentifier,
          "DBInstanceClass" -> dbInstanceClass,
          "DBName" -> dbName,
          "Engine" -> engine,
          "EngineVersion" -> engineVersion,
          "MasterUsername" -> masterUsername,
          "MasterUserPassword" -> masterPassword,
          "MultiAZ" -> multiAz,
          "Port" -> port,
          "PubliclyAccessible" -> publiclyAccessible,
          "StorageType" -> storageType,
          "VPCSecurityGroups" -> vpcSecurityGroups
        ).++(determineKmsKeyId).++(determineIops)
      )
    )
  })
}

case class RDSInstance(
                        configName: String = "",
                        dbInstanceIdentifier: String,
                        dbName: String,
                        allocatedStorage: Int,
                        dbInstanceClass: String,
                        engine: String,
                        engineVersion: String,
                        iops: Option[Int],
                        kmsKeyId: Option[String],
                        masterUsername: String,
                        masterPassword: String,
                        multiAz: Boolean,
                        port: String,
                        publiclyAccessible: Boolean,
                        storageType: String,
                        timezone: Option[String],
                        vpcSecurityGroups: List[String] = Nil,
                      ) extends Resource {

  val determineKmsKeyId: JsObject = {
    kmsKeyId.fold(JsObject(Nil))(arn => Json.obj("KmsKeyId" -> arn))
  }

  val determineIops: JsObject = {
    iops.fold(JsObject(Nil))(value => Json.obj( "iops" -> value))
  }
}

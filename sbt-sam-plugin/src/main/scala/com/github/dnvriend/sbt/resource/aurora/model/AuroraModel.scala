package com.github.dnvriend.sbt.resource.aurora.model

case class AvailabilityZones(zones: List[String])
case class BackupRetentionPeriod(value: Int)
case class DatabaseName(value: String)
case class DBClusterParameterGroupName(value: String)
case class DBSubnetGroupName(value: String)
case class Engine(value: String) {
  require(List("aurora", "aurora-postgresql").contains(value), s"Engine with value: '$value' should be either 'aurora' or 'aurora-postgresql'")
}
case class EngineVersion(value: String)
case class KmsKeyId(value: String)
case class MasterUsername(value: String)
case class MasterUserPassword(value: String)
case class Port(value: Integer)
case class PreferredBackupWindow(value: String)
case class PreferredMaintenanceWindow(value: String)
case class ReplicationSourceIdentifier(value: String)
case class SnapshotIdentifier(value: String)
case class StorageEncrypted(value: Boolean)
case class VpcSecurityGroupIds(ids: List[String])
case class AuroraCluster(
                          engine: Engine,
                          masterUsername: MasterUsername,
                          masterUserPassword: MasterUserPassword,
                          //                              availabilityZones: Option[AvailabilityZones],
                          //                              backupRetentionPeriod: Option[BackupRetentionPeriod],
                          //                              databaseName: Option[DatabaseName],
                          //                              dBClusterParameterGroupName: Option[DBClusterParameterGroupName],
                          //                              dBSubnetGroupName: Option[DBSubnetGroupName],
                          //                              engineVersion: Option[EngineVersion],
                          //                              kmsKeyId: Option[KmsKeyId],
                          //                              port: Option[Port],
                          //                              preferredBackupWindow: Option[PreferredBackupWindow],
                          //                              preferredMaintenanceWindow: Option[PreferredMaintenanceWindow],
                          //                              replicationSourceIdentifier: Option[ReplicationSourceIdentifier],
                          //                              snapshotIdentifier: Option[SnapshotIdentifier],
                          //                              storageEncrypted: Option[StorageEncrypted],
                          //                              vpcSecurityGroupIds: Option[VpcSecurityGroupIds]
                        )
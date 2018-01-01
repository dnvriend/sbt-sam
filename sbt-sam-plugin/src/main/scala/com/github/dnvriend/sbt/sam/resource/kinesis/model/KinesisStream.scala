package com.github.dnvriend.sbt.sam.resource.kinesis.model

case class KinesisStream(
    name: String,
    configName: String = "",
    // The number of hours for the data records that are stored in shards to remain accessible.
    // The default value is 24.
    retensionPeriodHours: Int = 24,
    // The number of shards that the stream uses. For greater provisioned throughput, increase the number of shards.
    shardCount: Int = 1,
    // Export the resource to other components. Default value is false
    export: Boolean = false
)
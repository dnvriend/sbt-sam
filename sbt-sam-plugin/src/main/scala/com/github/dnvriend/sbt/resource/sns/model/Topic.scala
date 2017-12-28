package com.github.dnvriend.sbt.resource.sns.model

case class Topic(
    name: String,
    configName: String = "",
    displayName: String = "",
    // Export the resource to other components. Default value is false
    export: Boolean = false
)
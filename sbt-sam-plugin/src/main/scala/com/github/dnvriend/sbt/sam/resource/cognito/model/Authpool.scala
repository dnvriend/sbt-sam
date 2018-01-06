package com.github.dnvriend.sbt.sam.resource.cognito.model

case class UserPoolUser(username: String, password: String)

case class ImportAuthPool(importResource: String)

case class Authpool(
    name: String,
    passwordPolicies: PasswordPolicies,
    users: List[UserPoolUser] = List.empty,
    export: Boolean = false
)
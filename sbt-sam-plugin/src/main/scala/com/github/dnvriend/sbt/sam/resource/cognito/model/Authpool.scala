package com.github.dnvriend.sbt.sam.resource.cognito.model

case class Authpool(
    name: String,
    passwordPolicies: PasswordPolicies,
    export: Boolean = false
)


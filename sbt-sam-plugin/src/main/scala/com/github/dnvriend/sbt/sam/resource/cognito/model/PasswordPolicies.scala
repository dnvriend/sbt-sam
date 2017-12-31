package com.github.dnvriend.sbt.sam.resource.cognito.model

case class PasswordPolicies(minimumLength: Int, requireLowercase: Boolean, requireNumbers: Boolean, requireSymbols: Boolean, requireUppercase: Boolean)
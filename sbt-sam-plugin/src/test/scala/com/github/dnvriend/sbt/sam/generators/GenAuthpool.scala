package com.github.dnvriend.sbt.sam.generators

import com.github.dnvriend.sbt.sam.resource.cognito.model.{ Authpool, PasswordPolicies }
import org.scalacheck.{ Arbitrary, Gen }

trait GenAuthpool extends GenGeneric {
  val genPasswordPolicies = for {
    minimumLength <- Gen.posNum[Int]
    requireLowercase <- Gen.oneOf(true, false)
    requireNumbers <- Gen.oneOf(true, false)
    requireSymbols <- Gen.oneOf(true, false)
    requireUppercase <- Gen.oneOf(true, false)
  } yield PasswordPolicies(
    minimumLength,
    requireLowercase,
    requireNumbers,
    requireSymbols,
    requireUppercase
  )
  val genAuthpool = for {
    name <- Gen.const("cognito-user-pool")
    policies <- genPasswordPolicies
  } yield Authpool(
    name,
    policies
  )

  implicit val arbAuthpool: Arbitrary[Authpool] = Arbitrary.apply(genAuthpool)

  val iterAuthpool: Iterator[Authpool] = iterFor(genAuthpool)
}
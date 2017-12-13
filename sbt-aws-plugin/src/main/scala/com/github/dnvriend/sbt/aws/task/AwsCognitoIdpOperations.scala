package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.cognitoidp.{ model, _ }
import com.amazonaws.services.cognitoidp.model._

import scala.collection.JavaConverters
import scala.util.Random
import scalaz.Disjunction

/**
 * add-custom-attributes                    | admin-add-user-to-group
 * admin-confirm-sign-up                    | admin-create-user
 * admin-delete-user                        | admin-delete-user-attributes
 * admin-disable-provider-for-user          | admin-disable-user
 * admin-enable-user                        | admin-forget-device
 * admin-get-device                         | admin-get-user
 * admin-initiate-auth                      | admin-link-provider-for-user
 * admin-list-devices                       | admin-list-groups-for-user
 * admin-remove-user-from-group             | admin-reset-user-password
 * admin-respond-to-auth-challenge          | admin-set-user-settings
 * admin-update-device-status               | admin-update-user-attributes
 * admin-user-global-sign-out               | change-password
 * confirm-device                           | confirm-forgot-password
 * confirm-sign-up                          | create-group
 * create-identity-provider                 | create-resource-server
 * create-user-import-job                   | create-user-pool
 * create-user-pool-client                  | create-user-pool-domain
 * delete-group                             | delete-identity-provider
 * delete-resource-server                   | delete-user
 * delete-user-attributes                   | delete-user-pool
 * delete-user-pool-client                  | delete-user-pool-domain
 * describe-identity-provider               | describe-resource-server
 * describe-user-import-job                 | describe-user-pool
 * describe-user-pool-client                | describe-user-pool-domain
 * forget-device                            | forgot-password
 * get-csv-header                           | get-device
 * get-group                                | get-identity-provider-by-identifier
 * get-ui-customization                     | get-user
 * get-user-attribute-verification-code     | global-sign-out
 * initiate-auth                            | list-devices
 * list-groups                              | list-identity-providers
 * list-resource-servers                    | list-user-import-jobs
 * list-user-pool-clients                   | list-user-pools
 * list-users                               | list-users-in-group
 * resend-confirmation-code                 | respond-to-auth-challenge
 * set-ui-customization                     | set-user-settings
 * sign-up                                  | start-user-import-job
 * stop-user-import-job                     | update-device-status
 * update-group                             | update-identity-provider
 * update-resource-server                   | update-user-attributes
 * update-user-pool                         | update-user-pool-client
 * verify-user-attribute                    | help
 */

object AwsCognitoIdpOperations {
  def client(cr: CredentialsAndRegion): AWSCognitoIdentityProvider = {
    AWSCognitoIdentityProviderClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }

  def adminCreateAndAuthUser(
    client: AWSCognitoIdentityProvider,
    userName: String,
    password: String,
    userPoolId: String,
    clientId: String): Disjunction[String, AdminRespondToAuthChallengeResult] = {
    // generate random temp password
    val tempPassword: String = Random.alphanumeric.take(10).mkString

    val adminCreateUserRequest: AdminCreateUserRequest = new AdminCreateUserRequest()
      .withUsername(userName)
      .withUserPoolId(userPoolId)
      .withTemporaryPassword(tempPassword)

    for {
      _ ← adminCreateUser(client, adminCreateUserRequest)
      adminInitiateAuthResult ← adminInitiateAuth(client, clientId, userPoolId, userName, tempPassword)
      adminRespondToAuthChallengeResult ← adminRespondToAuthChallenge(client, clientId, userPoolId, userName, password, adminInitiateAuthResult.getSession)
    } yield adminRespondToAuthChallengeResult
  }

  def adminCreateUser(
    client: AWSCognitoIdentityProvider,
    adminCreateUserRequest: AdminCreateUserRequest): Disjunction[String, AdminCreateUserResult] = {
    Disjunction.fromTryCatchNonFatal(client.adminCreateUser(adminCreateUserRequest)).leftMap(
      e ⇒ s"adminCreateUser with user ${adminCreateUserRequest.getUsername} error ==> ${e.toString}"
    )
  }

  def adminInitiateAuth(
    client: AWSCognitoIdentityProvider,
    clientId: String,
    userPoolId: String,
    userName: String,
    password: String): Disjunction[String, AdminInitiateAuthResult] = {
    val adminInitiateAuthRequest = new AdminInitiateAuthRequest()
      .withAuthFlow(model.AuthFlowType.ADMIN_NO_SRP_AUTH)
      .withClientId(clientId)
      .withUserPoolId(userPoolId)
      .withAuthParameters(JavaConverters.mapAsJavaMap(Map("USERNAME" → userName, "PASSWORD" → password)))

    Disjunction.fromTryCatchNonFatal(client.adminInitiateAuth(adminInitiateAuthRequest)).leftMap(
      e ⇒ s"adminInitiateAuth with user: $userName and tempPassword: $password error ==> ${e.toString}"
    )
  }

  def adminRespondToAuthChallenge(
    client: AWSCognitoIdentityProvider,
    clientId: String,
    userPoolId: String,
    userName: String,
    newPassword: String,
    session: String): Disjunction[String, AdminRespondToAuthChallengeResult] = {
    val adminRespondToAuthChallengeRequest: AdminRespondToAuthChallengeRequest = new AdminRespondToAuthChallengeRequest()
      .withClientId(clientId)
      .withUserPoolId(userPoolId)
      .withChallengeName("NEW_PASSWORD_REQUIRED")
      .addChallengeResponsesEntry("NEW_PASSWORD", newPassword)
      .addChallengeResponsesEntry("USERNAME", userName)
      .withSession(session)

    Disjunction.fromTryCatchNonFatal(client.adminRespondToAuthChallenge(adminRespondToAuthChallengeRequest)).leftMap(
      e ⇒ s"adminRespondToAuthChallenge with user: $userName and newPassword: $newPassword error ==> ${e.toString}"
    )
  }
}

case class CognitoUserDetails(userName: String, password: String, userPoolId: String, clientId: String)

case class ValidUser(userName: String, idToken: String)
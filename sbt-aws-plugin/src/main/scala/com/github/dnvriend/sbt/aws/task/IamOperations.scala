package com.github.dnvriend.sbt.aws.task

import com.amazonaws.auth.{ AWSCredentials, DefaultAWSCredentialsProviderChain }
import com.amazonaws.profile.path.cred.CredentialsDefaultLocationProvider
import com.amazonaws.regions.{ DefaultAwsRegionProviderChain, Regions }
import com.amazonaws.services.identitymanagement._
import com.amazonaws.services.identitymanagement.model.{ GetRolePolicyRequest, GetRoleRequest, User }
import com.github.dnvriend.ops.{ AllOps, AnyOps, Converter }
import com.github.dnvriend.sbt.aws.domain.IAMDomain.{ AwsCredentials, CredentialsProfileAndRegion, CredentialsRegionAndUser, ProfileLocation }
import sbt._

import scala.collection.JavaConverters._
import scala.util.matching.Regex
import scalaz.{ Show, _ }
import scalaz.Scalaz._

object Arn extends AnyOps {
  final val ArnRegex: Regex = """arn:(.*):(.*):(.*):(.*):(.*)/(.*)""".r
  implicit val fromArnString: Converter[String @@ Arn, Arn] =
    Converter.instance(arn => {
      arn.unwrap match {
        case arn @ ArnRegex(partition, service, region, accountId, resourceType, resource) =>
          Arn(
            Partition(partition),
            Service(service),
            Region(region),
            AccountId(accountId),
            ResourceType(resourceType),
            Resource(resource),
            arn
          )
      }
    })

  implicit val show: Show[Arn] = Show.shows(model => {
    import model._
    s"""
       |===================================================
       |Arn: $arnString
       |===================================================
       |Partition: ${partition.value}
       |Service: ${service.value}
       |Region: ${region.value}
       |AccountId: ${accountId.value}
       |ResourceType: ${resourceType.value}
       |Resource: ${resource.value}
     """.stripMargin
  })
}

final case class Arn(
    partition: Partition,
    service: Service,
    region: Region,
    accountId: AccountId,
    resourceType: ResourceType,
    resource: Resource,
    arnString: String)

final case class Partition(value: String)
final case class Service(value: String)
object Region {
  implicit val toRegions: Converter[Region, Regions] = {
    Converter.instance(region => Regions.fromName(region.value))
  }
}
final case class Region(value: String)
final case class AccountId(value: String)
final case class ResourceType(value: String)
final case class Resource(value: String)

object AmazonUser {
  implicit val show: Show[AmazonUser] = Show.shows(m => {
    s"""
      |==================
      |Amazon User
      |==================
      |UserName: ${m.user.getUserName}
      |UserId: ${m.user.getUserId}
      |AccountId: ${m.arn.accountId.value}
      |Arn: ${m.user.getArn}
      |Region: ${m.regions.getOrElse("No region")}
      |CreateDate: ${m.user.getCreateDate}
      |Password last used: ${m.user.getPasswordLastUsed}
    """.stripMargin
  })
}

final case class AmazonUser(user: User, arn: Arn, regions: Disjunction[Throwable, Regions])

/**
 * add-client-id-to-open-id-connect-provider | add-role-to-instance-profile
 * add-user-to-group                        | attach-group-policy
 * attach-role-policy                       | attach-user-policy
 * change-password                          | create-access-key
 * create-account-alias                     | create-group
 * create-instance-profile                  | create-login-profile
 * create-open-id-connect-provider          | create-policy
 * create-policy-version                    | create-role
 * create-saml-provider                     | create-service-linked-role
 * create-service-specific-credential       | create-user
 * create-virtual-mfa-device                | deactivate-mfa-device
 * delete-access-key                        | delete-account-alias
 * delete-account-password-policy           | delete-group
 * delete-group-policy                      | delete-instance-profile
 * delete-login-profile                     | delete-open-id-connect-provider
 * delete-policy                            | delete-policy-version
 * delete-role                              | delete-role-policy
 * delete-saml-provider                     | delete-ssh-public-key
 * delete-server-certificate                | delete-service-linked-role
 * delete-service-specific-credential       | delete-signing-certificate
 * delete-user                              | delete-user-policy
 * delete-virtual-mfa-device                | detach-group-policy
 * detach-role-policy                       | detach-user-policy
 * enable-mfa-device                        | generate-credential-report
 * get-access-key-last-used                 | get-account-authorization-details
 * get-account-password-policy              | get-account-summary
 * get-context-keys-for-custom-policy       | get-context-keys-for-principal-policy
 * get-credential-report                    | get-group
 * get-group-policy                         | get-instance-profile
 * get-login-profile                        | get-open-id-connect-provider
 * get-policy                               | get-policy-version
 * get-role                                 | get-role-policy
 * get-saml-provider                        | get-ssh-public-key
 * get-server-certificate                   | get-service-linked-role-deletion-status
 * get-user                                 | get-user-policy
 * list-access-keys                         | list-account-aliases
 * list-attached-group-policies             | list-attached-role-policies
 * list-attached-user-policies              | list-entities-for-policy
 * list-group-policies                      | list-groups
 * list-groups-for-user                     | list-instance-profiles
 * list-instance-profiles-for-role          | list-mfa-devices
 * list-open-id-connect-providers           | list-policies
 * list-policy-versions                     | list-role-policies
 * list-roles                               | list-saml-providers
 * list-ssh-public-keys                     | list-server-certificates
 * list-service-specific-credentials        | list-signing-certificates
 * list-user-policies                       | list-users
 * list-virtual-mfa-devices                 | put-group-policy
 * put-role-policy                          | put-user-policy
 * remove-client-id-from-open-id-connect-provider | remove-role-from-instance-profile
 * remove-user-from-group                   | reset-service-specific-credential
 * resync-mfa-device                        | set-default-policy-version
 * simulate-custom-policy                   | simulate-principal-policy
 * update-access-key                        | update-account-password-policy
 * update-assume-role-policy                | update-group
 * update-login-profile                     | update-open-id-connect-provider-thumbprint
 * update-role-description                  | update-saml-provider
 * update-ssh-public-key                    | update-server-certificate
 * update-service-specific-credential       | update-signing-certificate
 * update-user                              | upload-ssh-public-key
 * upload-server-certificate                | upload-signing-certificate
 */
object IamOperations extends AllOps {
  def client(): AmazonIdentityManagement = {
    AmazonIdentityManagementClientBuilder.defaultClient()
  }

  /**
   * Returns all users in the AWS account.
   */
  def listUsers(client: AmazonIdentityManagement): List[User] = {
    client.listUsers().getUsers.asScala.toList
  }

  /**
   * Returns the Username, UserId, Arn and CreateDate of the user based on the AWS access key ID used to sign the request to this API.
   */
  def getUser(client: AmazonIdentityManagement)(implicit
    arnConv: Converter[String @@ Arn, Arn],
    regionConv: Converter[Region, Regions]): AmazonUser = {
    val user: User = client.getUser.getUser
    val arn: Arn = arnConv(user.getArn.wrap[Arn])
    val regions: Disjunction[Throwable, Regions] = regionConv(arn.region).safe
    AmazonUser(user, arn, regions)
  }

  /**
   * Returns the inferred AWS Credentials and users using the DefaultAWSCredentialsProviderChain
   * and DefaultAwsRegionProviderChain used by the default AWS clients.
   */
  def getAwsCredentials(): Disjunction[Throwable, CredentialsProfileAndRegion] = Disjunction.fromTryCatchNonFatal {
    val profileLocation: File = new CredentialsDefaultLocationProvider().getLocation()
    val defaultCredsProvider = new DefaultAWSCredentialsProviderChain()
    val defaultRegionProvider = new DefaultAwsRegionProviderChain()
    val region: Regions = Regions.fromName(defaultRegionProvider.getRegion)
    val credentials: AWSCredentials = defaultCredsProvider.getCredentials
    CredentialsProfileAndRegion(
      AwsCredentials.fromAWSCredentials(credentials),
      ProfileLocation.fromFile(profileLocation),
      region
    )
  }

  /**
   * Returns the inferred AWS Credentials and users using the DefaultAWSCredentialsProviderChain
   * and DefaultAwsRegionProviderChain used by the default AWS clients.
   */
  def getAwsCredentialsAndUser(client: AmazonIdentityManagement): Disjunction[String, CredentialsRegionAndUser] = {
    val creds = getAwsCredentials.leftMap(_.getMessage).validationNel
    val user = Disjunction.fromTryCatchNonFatal(getUser(client)).leftMap(_.getMessage).validationNel
    (creds |@| user)((cred, user) => CredentialsRegionAndUser(cred, user.user)).leftMap(_.intercalate1(",")).disjunction
  }

  /**
   * Retrieves information about the specified role, including the role's path, GUID, ARN, and the role's trust policy
   * that grants permission to assume the role.
   */
  def getRole(roleName: String, client: AmazonIdentityManagement) = {
    Disjunction.fromTryCatchNonFatal(client.getRole(new GetRoleRequest().withRoleName(roleName))).toOption
  }
}

package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.codebuild._
import com.amazonaws.services.codebuild.model._
import com.github.dnvriend.ops.Converter

import scala.collection.JavaConverters._

import scalaz.Disjunction

//batch-delete-builds                      | batch-get-builds
//batch-get-projects                       | create-project
//create-webhook                           | delete-project
//delete-webhook                           | list-builds
//list-builds-for-project                  | list-curated-environment-images
//list-projects                            | start-build
//stop-build                               | update-project

final case class ProjectName(value: String)
final case class BuildId(value: String)
final case class BuildProjectName(value: String)

object StartBuildSettings {
  implicit val toRequest: Converter[StartBuildSettings, StartBuildRequest] =
    Converter.instance(settings => {
      new StartBuildRequest()
        .withProjectName(settings.projectName.value)
    })
}
final case class StartBuildSettings(projectName: ProjectName)

object StopBuildSettings {
  implicit val toRequest: Converter[StopBuildSettings, StopBuildRequest] =
    Converter.instance(settings => {
      new StopBuildRequest()
        .withId(settings.buildId.value)
    })
}
final case class StopBuildSettings(buildId: BuildId)

object ListBuildsSettings {
  implicit val toRequest: Converter[ListBuildsSettings, ListBuildsRequest] =
    Converter.instance(settings => {
      new ListBuildsRequest()
    })
}
final case class ListBuildsSettings()

object ListProjectsSettings {
  implicit val toRequest: Converter[ListProjectsSettings, ListProjectsRequest] =
    Converter.instance(settings => {
      new ListProjectsRequest()
    })
}
final case class ListProjectsSettings()

object CreateProjectSettings {
  implicit val toRequest: Converter[CreateProjectSettings, CreateProjectRequest] =
    Converter.instance(settings => {
      new CreateProjectRequest()
        .withName(settings.name.value)
    })
}
final case class CreateProjectSettings(name: BuildProjectName)

object DeleteProjectSettings {
  implicit val toRequest: Converter[DeleteProjectSettings, DeleteProjectRequest] =
    Converter.instance(settings => {
      new DeleteProjectRequest()
        .withName(settings.name.value)
    })
}
final case class DeleteProjectSettings(name: BuildProjectName)

object CreateWebhookSettings {
  implicit val toRequest: Converter[CreateWebhookSettings, CreateWebhookRequest] =
    Converter.instance(settings => {
      new CreateWebhookRequest()
        .withProjectName(settings.name.value)
    })
}
final case class CreateWebhookSettings(name: BuildProjectName)

object DeleteWebhookSettings {
  implicit val toRequest: Converter[DeleteWebhookSettings, DeleteWebhookRequest] =
    Converter.instance(settings => {
      new DeleteWebhookRequest()
        .withProjectName(settings.name.value)
    })
}
final case class DeleteWebhookSettings(name: BuildProjectName)

object CodeBuildOperations {
  def client(cr: CredentialsAndRegion): AWSCodeBuild = {
    AWSCodeBuildClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }

  /**
   * Start running the build
   */
  def startBuild(
    settings: StartBuildSettings,
    client: AWSCodeBuild)(implicit conv: Converter[StartBuildSettings, StartBuildRequest]): Disjunction[Throwable, StartBuildResult] = {
    Disjunction.fromTryCatchNonFatal(client.startBuild(conv(settings)))
  }

  /**
   * Attempts to stop the build
   */
  def stopBuild(
    settings: StopBuildSettings,
    client: AWSCodeBuild)(implicit conv: Converter[StopBuildSettings, StopBuildRequest]): Disjunction[Throwable, StopBuildResult] = {
    Disjunction.fromTryCatchNonFatal(client.stopBuild(conv(settings)))
  }

  /**
   * Gets a list of build IDs, with each build ID representing a single build
   */
  def listBuilds(
    settings: ListBuildsSettings,
    client: AWSCodeBuild)(implicit conv: Converter[ListBuildsSettings, ListBuildsRequest]): Disjunction[Throwable, ListBuildsResult] = {
    Disjunction.fromTryCatchNonFatal(client.listBuilds(conv(settings)))
  }

  /**
   * Gets a list of build project names, with each build project name representing a single build project.
   */
  def listProjects(
    settings: ListProjectsSettings,
    client: AWSCodeBuild)(implicit conv: Converter[ListProjectsSettings, ListProjectsRequest]): Disjunction[Throwable, ListProjectsResult] = {
    Disjunction.fromTryCatchNonFatal(client.listProjects(conv(settings)))
  }

  /**
   * Creates a build project
   */
  def createProjects(
    settings: CreateProjectSettings,
    client: AWSCodeBuild)(implicit conv: Converter[CreateProjectSettings, CreateProjectRequest]): Disjunction[Throwable, CreateProjectResult] = {
    Disjunction.fromTryCatchNonFatal(client.createProject(conv(settings)))
  }

  /**
   * Deletes a build project
   */
  def deleteProject(
    settings: DeleteProjectSettings,
    client: AWSCodeBuild)(implicit conv: Converter[DeleteProjectSettings, DeleteProjectRequest]): Disjunction[Throwable, DeleteProjectResult] = {
    Disjunction.fromTryCatchNonFatal(client.deleteProject(conv(settings)))
  }

  /**
   * For an existing AWS CodeBuild build project that has its source code stored in a GitHub repository,
   * enables AWS CodeBuild to begin automatically rebuilding the source code every time a code change is
   * pushed to the repository.
   */
  def createWebHook(
    settings: CreateWebhookSettings,
    client: AWSCodeBuild)(implicit conv: Converter[CreateWebhookSettings, CreateWebhookRequest]): Disjunction[Throwable, CreateWebhookResult] = {
    Disjunction.fromTryCatchNonFatal(client.createWebhook(conv(settings)))
  }

  /**
   * For an existing AWS CodeBuild build project that has its source code stored in a GitHub repository,
   * stops AWS CodeBuild from automatically rebuilding the source code every time a code change is pushed
   * to the repository.
   */
  def deleteWebHook(
    settings: DeleteWebhookSettings,
    client: AWSCodeBuild)(implicit conv: Converter[DeleteWebhookSettings, DeleteWebhookRequest]): Disjunction[Throwable, DeleteWebhookResult] = {
    Disjunction.fromTryCatchNonFatal(client.deleteWebhook(conv(settings)))
  }
}

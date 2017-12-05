package com.github.dnvriend.sbt.sam.state

import com.amazonaws.services.cloudformation.model.{AmazonCloudFormationException, DescribeStacksResult}
import com.github.dnvriend.ops.AllOps
import com.github.dnvriend.sbt.aws.task.Arn

import scalaz._

sealed trait SamState
case object CreateCloudFormationStack extends SamState
case object DeployS3Bucket extends SamState
case object DeployAWSResources extends SamState
case object DeployLambdas extends SamState
case object UpdateCloudFormation extends SamState

case object Deployed extends SamState

case class ProjectState(stackArn: Option[Arn] = None,
                         s3Bucket: Option[Arn] = None,
                          s3Jar: Option[Arn] = None,
                       )

object SamState extends AllOps {
  val nextState: PartialFunction[ProjectState, SamState] = {
    case ProjectState(None, None, None) => CreateCloudFormationStack
  }

  def determineState(stackName: String, result: Disjunction[Throwable, DescribeStacksResult]): ProjectState = result match {
    case \/-(stackResult) =>
      val stack = stackResult.getStacks.get(0)
      ProjectState(Option(Arn.fromArnString(stack.getStackId.wrap[Arn])), None, None)
    case -\/(t) if t.isInstanceOf[AmazonCloudFormationException] && t.getMessage.contains(s"Stack with id $stackName does not exist") =>
      ProjectState()
    case -\/(t) => throw t
  }
}

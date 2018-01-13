package com.github.dnvriend.sbt.sam.task

import java.lang.annotation.Annotation

import com.github.dnvriend.sbt.sam.cf.resource.lambda.event.s3.{S3EventType, S3Events}

import scala.tools.nsc.classpath.PackageNameUtils
import scalaz.Scalaz._

sealed trait LambdaHandler {
  def lambdaConfig: LambdaConfig
}

case class LambdaConfig(
                         cl: Class[_],
                         fqcn: String,
                         simpleClassName: String,
                         memorySize: Int = 1024,
                         timeout: Int = 300,
                         description: String = "",
                         managedPolicies: List[String] = List.empty,
                       )

case class HttpConf(
                     path: String = "/",
                     method: String = "get",
                     authorization: Boolean = false,
                   ) {
  require(path.startsWith("/"), s"field 'path', with value: '$path', must start with a '/'.")
  require(List("get", "put", "post", "patch", "options", "head", "delete").contains(method.toLowerCase), s"""Field 'method' with value '$method', must be one of "get", "put", "post", "patch", "options", "head", "delete".""")
}

case class DynamoConf(
                       tableName: String = "",
                       batchSize: Int = 100,
                       startingPosition: String = "LATEST",
                       enabled: Boolean = true,
                     )

final case class HttpHandler(
                              lambdaConfig: LambdaConfig,
                              httpConf: HttpConf
                            ) extends LambdaHandler

final case class DynamoHandler(
                                lambdaConfig: LambdaConfig,
                                dynamoConf: DynamoConf
                              ) extends LambdaHandler

case class ScheduleConf(
                         schedule: String
                       ) {
  // rate(1 minute)
  // rate(2 minutes)
  // rate(2 hours)
  // rate(2 days)
}

final case class ScheduledEventHandler(
                                        lambdaConfig: LambdaConfig,
                                        scheduleConf: ScheduleConf,
                                      ) extends LambdaHandler

case class SNSConf(topic: String)

final case class SNSEventHandler(
                                  lambdaConfig: LambdaConfig,
                                  snsConf: SNSConf
                                ) extends LambdaHandler

case class KinesisConf(
                        stream: String,
                        // TRIM_HORIZON or LATEST
                        startingPosition: String = "LATEST",
                        // Maximum number of stream records to process per function invocation
                        batchSize: Int = 100
                      )

final case class KinesisEventHandler(
                                      lambdaConfig: LambdaConfig,
                                      kinesisConf: KinesisConf
                                    ) extends LambdaHandler

case class S3Conf(
                   bucketResourceName: String ,
                   filter: String,
                   events: List[S3EventType]
                 )

final case class S3EventHandler(
                               lambdaConfig: LambdaConfig,
                               s3Conf: S3Conf
                               ) extends LambdaHandler

object ClassifyLambdas {
  def run(lambdas: Set[ProjectLambda],
          stage: String): Set[LambdaHandler] = {

    val dynamoHandlers: Set[LambdaHandler] = lambdas.map(_.projectClass.cl).filter(annotationPredicate("DynamoHandler"))
      .map(cl => (cl, cl.getName.withoutDollarSigns, cl.getSimpleName.withoutDollarSigns, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("DynamoHandler"))))
      .flatMap {
        case (cl, fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToDynamoHandler(cl, fqcn, simpleName, anno, stage))
      }

    val httpHandlers: Set[LambdaHandler] = lambdas.map(_.projectClass.cl).filter(annotationPredicate("HttpHandler"))
      .map(cl => (cl, cl.getName.withoutDollarSigns, cl.getSimpleName.withoutDollarSigns, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("HttpHandler"))))
      .flatMap {
        case (cl, fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToHttpHandler(cl, fqcn, simpleName, anno, stage))
      }

    val scheduledEventHandlers: Set[LambdaHandler] = lambdas.map(_.projectClass.cl).filter(annotationPredicate("ScheduleConf"))
      .map(cl => (cl, cl.getName.withoutDollarSigns, cl.getSimpleName.withoutDollarSigns, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("ScheduleConf"))))
      .flatMap {
        case (cl, fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToScheduledEventHandler(cl, fqcn, simpleName, anno, stage))
      }

    val snsEventHandlers: Set[LambdaHandler] = lambdas.map(_.projectClass.cl).filter(annotationPredicate("SNSConf"))
      .map(cl => (cl, cl.getName.withoutDollarSigns, cl.getSimpleName.withoutDollarSigns, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("SNSConf"))))
      .flatMap {
        case (cl, fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToSNSEventHandler(cl, fqcn, simpleName, anno, stage))
      }

    val kinesisEventHandlers: Set[LambdaHandler] = lambdas.map(_.projectClass.cl).filter(annotationPredicate("KinesisConf"))
      .map(cl => (cl, cl.getName.withoutDollarSigns, cl.getSimpleName.withoutDollarSigns, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("KinesisConf"))))
      .flatMap {
        case (cl, fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToKinesisEventHandler(cl, fqcn, simpleName, anno, stage))
      }

    val s3EventHandlers: Set[LambdaHandler] = lambdas.map(_.projectClass.cl).filter(annotationPredicate("S3Conf"))
      .map(cl => (cl, cl.getName.withoutDollarSigns, cl.getSimpleName.withoutDollarSigns, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("S3Conf"))))
      .flatMap {
        case (cl, fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToS3EventHandler(cl, fqcn, simpleName, anno, stage))
      }

    (dynamoHandlers ++ httpHandlers ++ scheduledEventHandlers ++ snsEventHandlers ++ kinesisEventHandlers ++ s3EventHandlers)
      .map(determinePolicies)
  }

  def determinePolicies(lambda: LambdaHandler): LambdaHandler = lambda match {
    case h: HttpHandler => h.copy(lambdaConfig = determinePoliciesForLambdaConfig(h.lambdaConfig))
    case h: DynamoHandler => h.copy(lambdaConfig = determinePoliciesForLambdaConfig(h.lambdaConfig))
    case h: ScheduledEventHandler => h.copy(lambdaConfig = determinePoliciesForLambdaConfig(h.lambdaConfig))
    case h: SNSEventHandler => h.copy(lambdaConfig = determinePoliciesForLambdaConfig(h.lambdaConfig))
    case h: KinesisEventHandler => h.copy(lambdaConfig = determinePoliciesForLambdaConfig(h.lambdaConfig))
    case h: S3EventHandler => h.copy(lambdaConfig = determinePoliciesForLambdaConfig(h.lambdaConfig))
  }

  def determinePoliciesForLambdaConfig(cfg: LambdaConfig): LambdaConfig = {
    val annotationNames = cfg.cl.getDeclaredAnnotations.toList.map(_.annotationType().getName)
      .map(PackageNameUtils.separatePkgAndClassNames)
      .map(_._2)
      .collect {
        case p@"AmazonDynamoDBFullAccess" => p
        case p@"AmazonKinesisFirehoseFullAccess" => p
        case p@"AmazonKinesisFullAccess" => p
        case p@"AmazonS3FullAccess" => p
        case p@"AmazonSNSFullAccess" => p
        case p@"AmazonVPCFullAccess" => p
        case p@"AWSKeyManagementServicePowerUser" => p
        case p@"AWSLambdaVPCAccessExecutionRole" => p
        case p@"AWSLambdaFullAccess" => p
        case p@"CloudWatchFullAccess" => p
        case p@"CloudWatchLogsFullAccess" => p
      }

    val policies: List[String] = annotationNames.toNel.map(_.toList).getOrElse(List(
      "AmazonDynamoDBFullAccess",
      "CloudWatchFullAccess",
      "CloudWatchLogsFullAccess",
      "AmazonSNSFullAccess",
      "AmazonKinesisFullAccess",
      "AWSKeyManagementServicePowerUser",
      "AmazonKinesisFirehoseFullAccess",
    ))

    cfg.copy(managedPolicies = policies)
  }

  def annotationPredicate(annotationName: String)(cl: Class[_]): Boolean = {
    cl.getDeclaredAnnotations.toList.exists(_.annotationType().getName.contains(annotationName))
  }

  def mapAnnoToDynamoHandler(cl: Class[_], fqcn: String, simpleName: String, anno: Annotation, stage: String): DynamoHandler = {
    val tableName = anno.annotationType().getMethod("tableName").invoke(anno).asInstanceOf[String]
    val batchSize = anno.annotationType().getMethod("batchSize").invoke(anno).asInstanceOf[Int]
    val startingPosition = anno.annotationType().getMethod("startingPosition").invoke(anno).asInstanceOf[String]
    val enabled = anno.annotationType().getMethod("enabled").invoke(anno).asInstanceOf[Boolean]
    val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
    val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
    val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]

    DynamoHandler(
      LambdaConfig(cl, fqcn, simpleName, memorySize, timeout, description),
      DynamoConf(tableName, batchSize, startingPosition, enabled)
    )
  }

  def mapAnnoToHttpHandler(cl: Class[_], className: String, simpleName: String, anno: Annotation, stage: String): HttpHandler = {
    val path = anno.annotationType().getMethod("path").invoke(anno).asInstanceOf[String]
    val method = anno.annotationType().getMethod("method").invoke(anno).asInstanceOf[String]
    val authorization = anno.annotationType().getMethod("authorization").invoke(anno).asInstanceOf[Boolean]
    val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
    val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
    val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]

    HttpHandler(
      LambdaConfig(cl, className, simpleName, memorySize, timeout, description),
      HttpConf(path, method, authorization)
    )
  }

  def mapAnnoToScheduledEventHandler(cl: Class[_], className: String, simpleName: String, anno: Annotation, stage: String): ScheduledEventHandler = {
    val schedule = anno.annotationType().getMethod("schedule").invoke(anno).asInstanceOf[String]
    val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
    val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
    val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]

    ScheduledEventHandler(
      LambdaConfig(cl, className, simpleName, memorySize, timeout, description),
      ScheduleConf(schedule)
    )
  }

  def mapAnnoToSNSEventHandler(cl: Class[_], className: String, simpleName: String, anno: Annotation, stage: String): SNSEventHandler = {
    val topic = anno.annotationType().getMethod("topic").invoke(anno).asInstanceOf[String]
    val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
    val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
    val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]

    SNSEventHandler(
      LambdaConfig(cl, className, simpleName, memorySize, timeout, description),
      SNSConf(topic)
    )
  }

  def mapAnnoToKinesisEventHandler(cl: Class[_], className: String, simpleName: String, anno: Annotation, stage: String): KinesisEventHandler = {
    val stream = anno.annotationType().getMethod("stream").invoke(anno).asInstanceOf[String]
    val batchSize = anno.annotationType().getMethod("batchSize").invoke(anno).asInstanceOf[Int]
    val startingPosition = anno.annotationType().getMethod("startingPosition").invoke(anno).asInstanceOf[String]
    val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
    val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
    val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]

    KinesisEventHandler(
      LambdaConfig(cl, className, simpleName, memorySize, timeout, description),
      KinesisConf(stream, startingPosition, batchSize)
    )
  }

  def mapAnnoToS3EventHandler(cl: Class[_], className:  String, simpleName: String, anno: Annotation, stage: String): S3EventHandler = {
    val bucketResourceName = anno.annotationType().getMethod("bucketResourceName").invoke(anno).asInstanceOf[String]
    val filter = anno.annotationType().getMethod("filter").invoke(anno).asInstanceOf[String]
    val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
    val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
    val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]
    val events = anno.annotationType().getMethod("events").invoke(anno).asInstanceOf[Array[String]].toList
    val s3Events = S3Events.fromList(events)

    S3EventHandler(
      LambdaConfig(cl, className, simpleName, memorySize, timeout, description),
      S3Conf(bucketResourceName, filter, s3Events)
    )
  }

  implicit class ClassOps(className: String) {
    def withoutDollarSigns: String = className.replace("$", "")
  }
}

package com.github.dnvriend.sbt.sam.task

import java.lang.annotation.Annotation

sealed trait LambdaHandler {
  def lambdaConfig: LambdaConfig
}

case class LambdaConfig(
                         fqcn: String,
                         simpleClassName: String,
                         memorySize: Int = 1024,
                         timeout: Int = 300,
                         description: String = ""
                       )

case class HttpConf(
                     path: String = "/",
                     method: String = "get",
                     authorization: Boolean = false,
                   )

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

object ClassifyLambdas {
  def run(lambdas: Set[ProjectLambda],
          stage: String): Set[LambdaHandler] = {

    val dynamoHandlers: Set[LambdaHandler] = lambdas.map(_.projectClass.cl).filter(annotationPredicate("DynamoHandler"))
      .map(cl => (cl.getName.withoutDollarSigns, cl.getSimpleName.withoutDollarSigns, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("DynamoHandler"))))
      .flatMap {
        case (fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToDynamoHandler(fqcn, simpleName, anno, stage))
      }

    val httpHandlers: Set[LambdaHandler] = lambdas.map(_.projectClass.cl).filter(annotationPredicate("HttpHandler"))
      .map(cl => (cl.getName.withoutDollarSigns, cl.getSimpleName.withoutDollarSigns, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("HttpHandler"))))
      .flatMap {
        case (fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToHttpHandler(fqcn, simpleName, anno, stage))
      }

    val scheduledEventHandlers: Set[LambdaHandler] = lambdas.map(_.projectClass.cl).filter(annotationPredicate("ScheduleConf"))
      .map(cl => (cl.getName.withoutDollarSigns, cl.getSimpleName.withoutDollarSigns, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("ScheduleConf"))))
      .flatMap {
        case (fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToScheduledEventHandler(fqcn, simpleName, anno, stage))
      }

    val snsEventHandlers: Set[LambdaHandler] = lambdas.map(_.projectClass.cl).filter(annotationPredicate("SNSConf"))
      .map(cl => (cl.getName.withoutDollarSigns, cl.getSimpleName.withoutDollarSigns, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("SNSConf"))))
      .flatMap {
        case (fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToSNSEventHandler(fqcn, simpleName, anno, stage))
      }

    val kinesisEventHandlers: Set[LambdaHandler] = lambdas.map(_.projectClass.cl).filter(annotationPredicate("KinesisConf"))
      .map(cl => (cl.getName.withoutDollarSigns, cl.getSimpleName.withoutDollarSigns, cl.getDeclaredAnnotations.find(_.annotationType().getName.contains("KinesisConf"))))
      .flatMap {
        case (fqcn, simpleName, annotations) => annotations.map(anno => mapAnnoToKinesisEventHandler(fqcn, simpleName, anno, stage))
      }


    dynamoHandlers ++ httpHandlers ++ scheduledEventHandlers ++ snsEventHandlers ++ kinesisEventHandlers
  }

  def annotationPredicate(annotationName: String)(cl: Class[_]): Boolean = {
    cl.getDeclaredAnnotations.toList.exists(_.annotationType().getName.contains(annotationName))
  }

  def mapAnnoToDynamoHandler(fqcn: String, simpleName: String, anno: Annotation, stage: String): DynamoHandler = {
    val tableName = anno.annotationType().getMethod("tableName").invoke(anno).asInstanceOf[String]
    val batchSize = anno.annotationType().getMethod("batchSize").invoke(anno).asInstanceOf[Int]
    val startingPosition = anno.annotationType().getMethod("startingPosition").invoke(anno).asInstanceOf[String]
    val enabled = anno.annotationType().getMethod("enabled").invoke(anno).asInstanceOf[Boolean]
    val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
    val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
    val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]

    DynamoHandler(
      LambdaConfig(fqcn, simpleName, memorySize, timeout, description),
      DynamoConf(tableName, batchSize, startingPosition, enabled)
    )
  }

  def mapAnnoToHttpHandler(className: String, simpleName: String, anno: Annotation, stage: String): HttpHandler = {
    val path = anno.annotationType().getMethod("path").invoke(anno).asInstanceOf[String]
    val method = anno.annotationType().getMethod("method").invoke(anno).asInstanceOf[String]
    val authorization = anno.annotationType().getMethod("authorization").invoke(anno).asInstanceOf[Boolean]
    val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
    val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
    val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]

    HttpHandler(
      LambdaConfig(className, simpleName, memorySize, timeout, description),
      HttpConf(path, method, authorization)
    )
  }

  def mapAnnoToScheduledEventHandler(className: String, simpleName: String, anno: Annotation, stage: String): ScheduledEventHandler = {
    val schedule = anno.annotationType().getMethod("schedule").invoke(anno).asInstanceOf[String]
    val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
    val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
    val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]

    ScheduledEventHandler(
      LambdaConfig(className, simpleName, memorySize, timeout, description),
      ScheduleConf(schedule)
    )
  }

  def mapAnnoToSNSEventHandler(className: String, simpleName: String, anno: Annotation, stage: String): SNSEventHandler = {
    val topic = anno.annotationType().getMethod("topic").invoke(anno).asInstanceOf[String]
    val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
    val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
    val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]

    SNSEventHandler(
      LambdaConfig(className, simpleName, memorySize, timeout, description),
      SNSConf(topic)
    )
  }

  def mapAnnoToKinesisEventHandler(className: String, simpleName: String, anno: Annotation, stage: String): KinesisEventHandler = {
    val stream = anno.annotationType().getMethod("stream").invoke(anno).asInstanceOf[String]
    val batchSize = anno.annotationType().getMethod("batchSize").invoke(anno).asInstanceOf[Int]
    val startingPosition = anno.annotationType().getMethod("startingPosition").invoke(anno).asInstanceOf[String]
    val memorySize = anno.annotationType().getMethod("memorySize").invoke(anno).asInstanceOf[Int]
    val timeout = anno.annotationType().getMethod("timeout").invoke(anno).asInstanceOf[Int]
    val description = anno.annotationType().getMethod("description").invoke(anno).asInstanceOf[String]

    KinesisEventHandler(
      LambdaConfig(className, simpleName, memorySize, timeout, description),
      KinesisConf(stream, startingPosition, batchSize)
    )
  }

  implicit class ClassOps(className: String) {
    def withoutDollarSigns: String = className.replace("$", "")
  }
}

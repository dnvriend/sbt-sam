package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.cloudwatch._
import com.amazonaws.services.cloudwatch.model.Metric

import scala.collection.JavaConverters._

/**
  * delete-alarms                            | delete-dashboards
  * describe-alarm-history                   | describe-alarms
  * describe-alarms-for-metric               | disable-alarm-actions
  * enable-alarm-actions                     | get-dashboard
  * get-metric-statistics                    | list-dashboards
  * list-metrics                             | put-dashboard
  * put-metric-alarm                         | put-metric-data
  * set-alarm-state                          | wait
  */

final case class LambdaMetrics(errors: List[Metric],
                               invocations: List[Metric],
                               durations: List[Metric],
                               throttles: List[Metric],
                              )

object CloudWatchOperations {
  final val Namespacelambda = "AWS/Lambda"
  final val MetricDuration = "Duration"
  final val MetricInvocations = "Invocations"
  final val MetricThrottles = "Throttles"
  final val MetricErrors = "Errors"

  val base = (m: Metric) => m.getNamespace == Namespacelambda
  val invocations = (m: Metric) => base(m) && m.getMetricName == MetricInvocations
  val durations = (m: Metric) => base(m) && m.getMetricName == MetricDuration
  val throttles = (m: Metric) => base(m) && m.getMetricName == MetricThrottles
  val errors = (m: Metric) => base(m) && m.getMetricName == MetricErrors

  def client(cr: CredentialsAndRegion): AmazonCloudWatch = {
    AmazonCloudWatchClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }

  def listMetrics(client: AmazonCloudWatch): List[Metric] = {
    client.listMetrics().getMetrics.asScala.toList
  }

  def lambdaMetrics(client: AmazonCloudWatch): LambdaMetrics = {
    val metrics: List[Metric] = listMetrics(client)
    LambdaMetrics(
      metrics.filter(errors),
      metrics.filter(invocations),
      metrics.filter(durations),
      metrics.filter(throttles)
    )
  }
}

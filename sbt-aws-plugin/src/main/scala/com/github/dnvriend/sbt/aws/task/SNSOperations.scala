package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.sns._

/**
 * add-permission                           | check-if-phone-number-is-opted-out
 * confirm-subscription                     | create-platform-application
 * create-platform-endpoint                 | create-topic
 * delete-endpoint                          | delete-platform-application
 * delete-topic                             | get-endpoint-attributes
 * get-platform-application-attributes      | get-sms-attributes
 * get-subscription-attributes              | get-topic-attributes
 * list-endpoints-by-platform-application   | list-phone-numbers-opted-out
 * list-platform-applications               | list-subscriptions
 * list-subscriptions-by-topic              | list-topics
 * opt-in-phone-number                      | publish
 * remove-permission                        | set-endpoint-attributes
 * set-platform-application-attributes      | set-sms-attributes
 * set-subscription-attributes              | set-topic-attributes
 * subscribe                                | unsubscribe
 */
object SNSOperations {
  def client(): AmazonSNS = {
    AmazonSNSClientBuilder.defaultClient()
  }
}

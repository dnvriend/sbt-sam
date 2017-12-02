package com.github.dnvriend.sbt.aws.task

import com.amazonaws.services.apigateway._
import com.github.dnvriend.ops.Converter

import scala.collection.JavaConverters._

/**
 * create-api-key                           | create-authorizer
 * create-base-path-mapping                 | create-deployment
 * create-documentation-part                | create-documentation-version
 * create-domain-name                       | create-model
 * create-request-validator                 | create-resource
 * create-rest-api                          | create-stage
 * create-usage-plan                        | create-usage-plan-key
 * delete-api-key                           | delete-authorizer
 * delete-base-path-mapping                 | delete-client-certificate
 * delete-deployment                        | delete-documentation-part
 * delete-documentation-version             | delete-domain-name
 * delete-gateway-response                  | delete-integration
 * delete-integration-response              | delete-method
 * delete-method-response                   | delete-model
 * delete-request-validator                 | delete-resource
 * delete-rest-api                          | delete-stage
 * delete-usage-plan                        | delete-usage-plan-key
 * flush-stage-authorizers-cache            | flush-stage-cache
 * generate-client-certificate              | get-account
 * get-api-key                              | get-api-keys
 * get-authorizer                           | get-authorizers
 * get-base-path-mapping                    | get-base-path-mappings
 * get-client-certificate                   | get-client-certificates
 * get-deployment                           | get-deployments
 * get-documentation-part                   | get-documentation-parts
 * get-documentation-version                | get-documentation-versions
 * get-domain-name                          | get-domain-names
 * get-export                               | get-gateway-response
 * get-gateway-responses                    | get-integration
 * get-integration-response                 | get-method
 * get-method-response                      | get-model
 * get-model-template                       | get-models
 * get-request-validator                    | get-request-validators
 * get-resource                             | get-resources
 * get-rest-api                             | get-rest-apis
 * get-sdk                                  | get-sdk-type
 * get-sdk-types                            | get-stage
 * get-stages                               | get-usage
 * get-usage-plan                           | get-usage-plan-key
 * get-usage-plan-keys                      | get-usage-plans
 * import-api-keys                          | import-documentation-parts
 * import-rest-api                          | put-gateway-response
 * put-integration                          | put-integration-response
 * put-method                               | put-method-response
 * put-rest-api                             | test-invoke-authorizer
 * test-invoke-method                       | update-account
 * update-api-key                           | update-authorizer
 * update-base-path-mapping                 | update-client-certificate
 * update-deployment                        | update-documentation-part
 * update-documentation-version             | update-domain-name
 * update-gateway-response                  | update-integration
 * update-integration-response              | update-method
 * update-method-response                   | update-model
 * update-request-validator                 | update-resource
 * update-rest-api                          | update-stage
 * update-usage                             | update-usage-plan
 */

object ApiGatewayOperations {
  def client(cr: CredentialsAndRegion): AmazonApiGateway = {
    AmazonApiGatewayClientBuilder.standard()
      .withRegion(cr.region)
      .withCredentials(cr.credentialsProvider)
      .build()
  }
}

/*
 * Copyright 2017 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend.lambda.annotation.policy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Grants permissions for Amazon Elastic Compute Cloud (Amazon EC2) actions to manage elastic network interfaces (ENIs). If you are writing a Lambda function to access resources in a VPC in the Amazon Virtual Private Cloud (Amazon VPC) service, you can attach this permissions policy. The policy also grants permissions for CloudWatch Logs actions to write logs.
 *
 You can find these AWS managed permissions policies in the IAM console. Search for these policies and you can see the permissions each of these policies grant.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AWSLambdaVPCAccessExecutionRole {
}

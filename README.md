![Logo image](img/sbtscalasamlogo_small.png)

# sbt-serverless-plugin
[![Build Status](https://travis-ci.org/dnvriend/sbt-sam.svg?branch=master)](https://travis-ci.org/dnvriend/sbt-sam)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/36f71fd0aff3488a922db7f8e0c9008b)](https://www.codacy.com/app/dnvriend/sbt-sam?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dnvriend/sbt-sam&amp;utm_campaign=Badge_Grade)
[![Download](https://api.bintray.com/packages/dnvriend/maven/sbt-sam/images/download.svg) ](https://bintray.com/dnvriend/maven/sbt-sam/_latestVersion)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

## Configuration
The standard resolution for AWS credentials is:

- Environment Variables,
- Java System Properties,
- The AWS credentials file,
- The AWS shared config file,
- Container credentials,
- Instance profile credentials

The following environment variables can be set in the environment eg. build server to set the profile, region and api keys to use:

- __AWS_PROFILE__ - The aws profile to use
- __AWS_ACCESS_KEY_ID__ - the aws access key to use
- __AWS_SECRET_ACCESS_KEY__ - the aws secret access key to use
- __AWS_REGION__ - the aws region to use

The following Java System Properties can be set:

- __aws.accessKeyId__ - the aws access key to use
- __aws.secretKey__ - the aws secret access key to use
- __aws.region__ - the aws region to use

The 'default credential profiles' file:
- `~/.aws/credentials`
- Place your credentials (API keys here)
- aws_access_key_id
- aws_secret_access_key
- the file can be set with higher restrictions
- AWS clients will only read credential keys here

The 'default cli config' file:
- `~/.aws/config`
- Place to put credentials and other things
- Don't put credentials here, but you could
- Supported settings are:
- aws_access_key_id:  AWS access key
- aws_secret_access_key: AWS secret key
- aws_session_token: AWS session token. A session token is only required if you are using temporary security credentials.
- region: the aws region to use by the client
- output: output format (json, text, or table)

## Videos
- [AWS - Building a Development Workflow for Serverless Applications (March - 2017)](https://www.youtube.com/watch?v=e3lreqpWN0A)
- [AWS - Local Testing and Deployment Best Practices for Serverless Applications](https://www.youtube.com/watch?v=QRSc1dL-I4U)
- [AWS - Security Best Practices for Serverless Applications - 2017 AWS Online Tech Talks](https://www.youtube.com/watch?v=AV24RTvbgWA)
- [AWS - Serverless Orchestration with AWS Step Functions](https://www.youtube.com/watch?v=8rmgF-SbcIk)
- [AWS -  Serverless Architectural Patterns and Best Practices](https://www.youtube.com/watch?v=b7UMoc1iUYw)
- [AWS - Optimizing the Data Tier for Serverless Web Applications](https://www.youtube.com/watch?v=BG_xi6ACm5I)

## Resources
- [Github - AWS SAM](https://github.com/awslabs/serverless-application-model)
- [AWS - Deploying Lambda-based Applications](http://docs.aws.amazon.com/lambda/latest/dg/deploying-lambda-apps.html)
- [AWS - CloudFormation concepts](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-whatis-concepts.html#d0e3897)
- http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
- http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html
- http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
- http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-region-selection.html
- http://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys
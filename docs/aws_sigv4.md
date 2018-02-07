# Aws sigv4
Signature Version 4 is the process to add authentication information to AWS requests. For security, most requests to AWS 
must be signed with an access key, which consists of an access key ID and secret access key.

## Current Situation
- sam allows for having a local authpoolName called 'auth_pool' or one that is imported
- sam allows for a lambda to define whether it is secured or not with a boolean flag and 'authorized' field, 
  This field means that authorization is necessary, but if set, sam assumes that an cognito user pool is used,
  as that is the only authorization method it supports at this time. So when authorization is required on a lambda,
  all lambdas use the sam local 'auth_pool' or all use the imported authPool with a the given name.
  
## Reason for change
- IAM defines uses and allows for the creation of API keys. These keys can be used to sign API requests using 
  [Signature Version 4](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html) - awsSigv4
- Having sigv4 allows for simpler authentication for simple back-end APIs and does not need a Cognito User Pool.

## Feature after change
- sam allows for setting an authorization method per lambda
- per lambda it should be possible to choose the [authtype](https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-swagger-extensions-authtype.html)
- the authtype is set to none by default, but can be set to a name
  - the name points to an authorizer with a specific name
  - names: sigv4
  - 

## SwaggerDefinitionBody
It is possible to define multiple security objects inside 'securityDefinitions'

## resources
- https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-swagger-extensions-authtype.html
- https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html
- https://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html
- https://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html
- https://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
- https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-examples-using-sdks.html
- https://docs.aws.amazon.com/apigateway/api-reference/signing-requests/

package com.github.dnvriend.lambda.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Generic Lambda Configuration. A Generic Lambda won't be wired by the Serverless Application Model to
 * resources like eg. a scheduler, DynamoDB or a Kinesis stream, but will only be deployed by SAM to AWS
 * and assigned an ARN. The lambda must then be manually invoked by a resource that has been given access
 * to invoke the lambda, eg. AWS RDS Aurora for MySQL
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GenericConf {
    int memorySize() default 1024;
    int timeout() default 300;
    String description() default "";
}

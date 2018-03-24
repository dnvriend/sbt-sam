package com.github.dnvriend.lambda.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SNSConf {
    String topic() default "";
    int memorySize() default 1024;
    int timeout() default 300;
    String description() default "";
    int reservedConcurrentExecutions() default -1;
}

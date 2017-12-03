package com.github.dnvriend.lambda.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DynamoHandler {
    String tableName() default "";
    int batchSize() default 100;
    String startingPosition() default "LATEST";
    boolean enabled() default true;
    String name() default "";
    int memorySize() default 1024;
    int timeout() default 300;
    String description() default "";
}

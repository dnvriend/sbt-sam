package com.github.dnvriend.lambda.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface HttpHandler {
    String path() default "/";
    String method() default "get";
    boolean authorization() default false;
    int memorySize() default 1024;
    int timeout() default 300;
    String description() default "";
    int reservedConcurrentExecutions() default -1;
}

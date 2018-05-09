package com.github.dnvriend.sam.stepfunctions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface StepFunctionConf {
    String stateName();
    Class<?> stateMachine();
    int memorySize() default 1024;
    int timeout() default 300;
    String description() default "";
    int reservedConcurrentExecutions() default -1;
}

package com.github.dnvriend.lambda.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface VPCConf {
    String[] securityGroupIds() default {};
    String[] subnetIds() default {};
}

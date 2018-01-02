package com.github.dnvriend.lambda.annotation.imports;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ImportAuthorization {
    String name() default "";
}

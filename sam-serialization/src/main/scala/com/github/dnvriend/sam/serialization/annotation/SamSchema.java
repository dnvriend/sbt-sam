package com.github.dnvriend.sam.serialization.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SamSchema {
    String name() default "";
    String description() default "";
}

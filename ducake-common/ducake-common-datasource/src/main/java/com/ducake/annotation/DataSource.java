package com.ducake.annotation;

import java.lang.annotation.*;

/**
 * @author 93477
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DataSource {
    String value() default "default";
}

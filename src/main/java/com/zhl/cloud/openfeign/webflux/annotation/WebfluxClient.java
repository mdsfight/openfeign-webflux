package com.zhl.cloud.openfeign.webflux.annotation;

import java.lang.annotation.*;

/**
 * @author zhl
 * @date 2025/5/6 9:57:59
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface WebfluxClient {

    String name();

    String url() default "";

    long connectTimeout() default 3000;

    long readTimeout() default 5000;


}

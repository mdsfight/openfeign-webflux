package com.zhl.cloud.openfeign.webflux.annotation;

import com.zhl.cloud.openfeign.webflux.registrar.WebClientRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author zhl
 * @date 2025/5/6 9:59:42
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(WebClientRegister.class)
public @interface EnableWebClient {
}

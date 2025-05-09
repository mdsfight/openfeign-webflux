package com.zhl.cloud.openfeign.webflux.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author zhl
 * @date 2025/5/6 13:27:54
 */
@Getter
public class WebfluxClientException extends RuntimeException {

    private final HttpStatus status;

    public WebfluxClientException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}

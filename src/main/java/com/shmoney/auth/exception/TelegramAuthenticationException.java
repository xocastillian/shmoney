package com.shmoney.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TelegramAuthenticationException extends RuntimeException {
    public TelegramAuthenticationException(String message) {
        super(message);
    }

    public TelegramAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

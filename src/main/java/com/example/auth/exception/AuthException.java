package com.example.auth.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public AuthException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
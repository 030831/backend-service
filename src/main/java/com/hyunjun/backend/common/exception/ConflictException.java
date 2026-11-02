package com.hyunjun.backend.common.exception;

/**
 * Http 409 - 요청이 지금 상태와 충돌한다.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message,Throwable cause) {
        super(message, cause);
    }
}

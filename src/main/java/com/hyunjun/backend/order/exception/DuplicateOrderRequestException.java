package com.hyunjun.backend.order.exception;

public class DuplicateOrderRequestException extends RuntimeException {
    public DuplicateOrderRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}

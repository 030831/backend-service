package com.hyunjun.backend.order.exception;

import com.hyunjun.backend.common.exception.ConflictException;

public class InvalidOrderStateException extends ConflictException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}

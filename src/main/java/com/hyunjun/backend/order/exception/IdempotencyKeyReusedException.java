package com.hyunjun.backend.order.exception;

import com.hyunjun.backend.common.exception.ConflictException;

public class IdempotencyKeyReusedException extends ConflictException {
    public IdempotencyKeyReusedException(String message) {
        super(message);
    }
}

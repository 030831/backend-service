package com.hyunjun.backend.product.exception;

import com.hyunjun.backend.common.exception.ConflictException;

public class InsufficientStockException extends ConflictException {
    public InsufficientStockException(String message) {
        super(message);
    }
}

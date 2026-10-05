package com.hyunjun.backend.order.exception;

import com.hyunjun.backend.common.exception.ConflictException;

public class PriceChangedException extends ConflictException {
    public PriceChangedException(String message) {
        super(message);
    }
}

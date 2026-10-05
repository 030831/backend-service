package com.hyunjun.backend.order.exception;

import com.hyunjun.backend.common.exception.NotFoundException;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}

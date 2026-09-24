package com.hyunjun.backend.product.exception;

import com.hyunjun.backend.common.exception.NotFoundException;

public class SkuNotFoundException extends NotFoundException {
    public SkuNotFoundException(String message) {
        super(message);
    }
}

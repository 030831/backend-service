package com.hyunjun.backend.product.exception;

import com.hyunjun.backend.common.exception.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}

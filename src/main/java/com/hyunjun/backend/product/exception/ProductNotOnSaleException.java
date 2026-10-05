package com.hyunjun.backend.product.exception;

import com.hyunjun.backend.common.exception.ConflictException;

public class ProductNotOnSaleException extends ConflictException {
    public ProductNotOnSaleException(String message) {
        super(message);
    }
}

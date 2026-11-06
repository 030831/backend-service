package com.hyunjun.backend.cart.exception;

import com.hyunjun.backend.common.exception.ConflictException;

// "장바구에는 판매 단위마다 100개까지 담을 수 있습니다."
public class CartQuantityExceededException extends ConflictException {
    public CartQuantityExceededException(String message) {
        super(message);
    }
}

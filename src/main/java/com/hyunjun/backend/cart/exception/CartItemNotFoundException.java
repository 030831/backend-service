package com.hyunjun.backend.cart.exception;

import com.hyunjun.backend.common.exception.NotFoundException;

// "장바구니 항목을 찾을 수 없습니다."
public class CartItemNotFoundException extends NotFoundException {
    public CartItemNotFoundException(String message) {
        super(message);
    }
}

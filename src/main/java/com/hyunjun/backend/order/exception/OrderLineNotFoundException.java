package com.hyunjun.backend.order.exception;

import com.hyunjun.backend.common.exception.NotFoundException;

// "주문 항목을 찾을 수 없습니다."
public class OrderLineNotFoundException extends NotFoundException {
    public OrderLineNotFoundException(String message) {
        super(message);
    }
}

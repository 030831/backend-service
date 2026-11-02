package com.hyunjun.backend.order.exception;

import com.hyunjun.backend.common.exception.ConflictException;

// "결제 금액이 다릅니다."
public class PaymentAmountMismatchException extends ConflictException {
    public PaymentAmountMismatchException(String message) {
        super(message);
    }
}

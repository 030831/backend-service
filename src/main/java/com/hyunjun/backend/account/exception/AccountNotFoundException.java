package com.hyunjun.backend.account.exception;

import com.hyunjun.backend.common.exception.NotFoundException;

public class AccountNotFoundException extends NotFoundException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

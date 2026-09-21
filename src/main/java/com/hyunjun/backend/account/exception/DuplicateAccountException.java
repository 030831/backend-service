package com.hyunjun.backend.account.exception;

import com.hyunjun.backend.common.exception.ConflictException;

public class DuplicateAccountException extends ConflictException {
    public DuplicateAccountException(String message) {
        super(message);
    }

    public DuplicateAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}

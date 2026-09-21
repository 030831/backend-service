package com.hyunjun.backend.account.exception;

public class DuplicateAccountException extends RuntimeException {
    public DuplicateAccountException(String message) {
        super(message);
    }

    public DuplicateAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}

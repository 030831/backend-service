package com.hyunjun.backend.common.exception;

public class CommerceNotFoundException extends RuntimeException {
    public CommerceNotFoundException(String message) {
        super(message);
    }
}

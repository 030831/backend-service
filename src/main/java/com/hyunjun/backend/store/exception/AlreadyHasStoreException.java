package com.hyunjun.backend.store.exception;

import com.hyunjun.backend.common.exception.ConflictException;

public class AlreadyHasStoreException extends ConflictException {
    public AlreadyHasStoreException(String message) {
        super(message);
    }
}

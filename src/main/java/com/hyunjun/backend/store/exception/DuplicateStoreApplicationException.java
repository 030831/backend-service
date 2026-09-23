package com.hyunjun.backend.store.exception;

import com.hyunjun.backend.common.exception.ConflictException;

public class DuplicateStoreApplicationException extends ConflictException {
    public DuplicateStoreApplicationException(String message) {
        super(message);
    }
}

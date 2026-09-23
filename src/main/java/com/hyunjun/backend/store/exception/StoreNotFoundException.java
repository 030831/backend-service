package com.hyunjun.backend.store.exception;

import com.hyunjun.backend.common.exception.NotFoundException;

public class StoreNotFoundException extends NotFoundException {
    public StoreNotFoundException(String message) {
        super(message);
    }
}

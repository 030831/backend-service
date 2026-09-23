package com.hyunjun.backend.store.exception;

import com.hyunjun.backend.common.exception.NotFoundException;

public class StoreApplicationNotFoundException extends NotFoundException {
    public StoreApplicationNotFoundException(String message) {
        super(message);
    }
}

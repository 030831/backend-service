package com.hyunjun.backend.admin.exception;

import com.hyunjun.backend.common.exception.NotFoundException;

public class AdminAccountNotFoundException extends NotFoundException {
    public AdminAccountNotFoundException(String message) {
        super(message);
    }
}

package com.hyunjun.backend.store.exception;

import com.hyunjun.backend.common.exception.ConflictException;

public class AlreadyReviewedApplicationException extends ConflictException {
    public AlreadyReviewedApplicationException(String message) {
        super(message);
    }
}

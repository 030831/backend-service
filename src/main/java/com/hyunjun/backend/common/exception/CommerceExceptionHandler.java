package com.hyunjun.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommerceExceptionHandler {

    @ExceptionHandler(CommerceConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String conflict(CommerceConflictException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(CommerceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(CommerceNotFoundException exception) {
        return exception.getMessage();
    }
}

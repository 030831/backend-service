package com.hyunjun.backend.account.exception;

import com.hyunjun.backend.account.controller.AccountController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AccountController.class)
public class AccountExceptionHandler {

    @ExceptionHandler(DuplicateAccountException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateAccount(DuplicateAccountException exception) {
        return exception.getMessage();
    }
}

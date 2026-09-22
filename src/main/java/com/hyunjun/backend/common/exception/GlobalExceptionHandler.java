package com.hyunjun.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(org.springframework.security.core.AuthenticationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}

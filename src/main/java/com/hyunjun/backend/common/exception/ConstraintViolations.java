package com.hyunjun.backend.common.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

public final class ConstraintViolations  {

    private ConstraintViolations() { }

    public static boolean isViolated(DataIntegrityViolationException exception,
                                     String constraintName) {

        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {

                String violatedName = violation.getConstraintName();
                if (violatedName != null && violatedName.contains(constraintName)) {
                    return true;
                }
            }

            cause = cause.getCause();
        }

        return false;
    }

}

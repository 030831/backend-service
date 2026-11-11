package com.hyunjun.backend.common.domain;

public final class DomainText {

    private DomainText() { }

    public static String required(String value, int maxLength, String fieldName) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + "은(는) 비어 있지 않은 " + maxLength + "자 이하여야 합니다."
            );
        }

        return value;
    }

    public static String optional(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + "은(는) " + maxLength + "자 이하여야 합니다."
            );
        }

        return value;
    }
}

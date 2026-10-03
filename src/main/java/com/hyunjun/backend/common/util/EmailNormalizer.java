package com.hyunjun.backend.common.util;

import java.util.Locale;

public final class EmailNormalizer {

    private EmailNormalizer() {

    }

    /**
     * 앞뒤 공백을 지우고 소문자로 바꾼다. null이면 null.
     **/
    public static String normalize(String email) {
        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}

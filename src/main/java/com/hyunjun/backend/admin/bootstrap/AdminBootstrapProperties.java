package com.hyunjun.backend.admin.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.bootstrap")
public record AdminBootstrapProperties(String email, String name, String password) {

    public boolean isConfigured() {
        return hasText(email) && hasText(name) && hasText(password);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

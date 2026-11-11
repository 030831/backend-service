package com.hyunjun.backend.common.security;

import java.time.Instant;

public interface LoginCredential {

    Long getId();

    String getPasswordHash();

    boolean isLoginLocked(Instant now);
}

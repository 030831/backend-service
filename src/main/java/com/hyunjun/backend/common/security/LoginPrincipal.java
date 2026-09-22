package com.hyunjun.backend.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.List;

public record LoginPrincipal(LoginType type, Long id) implements Serializable {

    public static LoginPrincipal account(Long accountId) {
        return new LoginPrincipal(LoginType.ACCOUNT, accountId);
    }

    public static LoginPrincipal admin(Long adminId) {
        return new LoginPrincipal(LoginType.ADMIN, adminId);
    }

    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority(type.name()));
    }
}

package com.hyunjun.backend.common.security;

import com.hyunjun.backend.common.util.EmailNormalizer;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

public abstract class PasswordLoginProvider implements AuthenticationProvider {

    private static final String BAD_CREDENTIALS = "이메일 또는 비밀번호가 올바르지 않습니다.";

    private final PasswordEncoder passwordEncoder;

    private final String dummyPasswordHash;

    protected PasswordLoginProvider(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Override
    public final Authentication authenticate(Authentication authentication) {
        String email = EmailNormalizer.normalize(authentication.getName());
        String rawPassword = String.valueOf(authentication.getCredentials());
        Instant now = Instant.now();

        LoginCredential credential = findByEmail(email).orElse(null);

        if (credential == null) {
            passwordEncoder.matches(rawPassword, dummyPasswordHash); // 결과는 버리고 시간만 맞춘다.
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        if (credential.isLoginLocked(now)) {
            throw new LockedException("로그인 시도가 너무 많습니다. 잠시 후 다시 시도하세요.");
        }

        if (!passwordEncoder.matches(rawPassword, credential.getPasswordHash())) {
            recordFailure(credential.getId(), now);
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        recordSuccess(credential.getId());

        LoginPrincipal principal = principalOf(credential.getId());

        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.authorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected abstract Optional<? extends LoginCredential> findByEmail(String email);

    protected abstract void recordFailure(Long id, Instant now);

    protected abstract void recordSuccess(Long id);

    protected abstract LoginPrincipal principalOf(Long id);
}

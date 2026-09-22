package com.hyunjun.backend.account.security;

import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.account.repository.AccountRepository;
import com.hyunjun.backend.common.security.LoginPrincipal;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountAuthenticationProvider implements AuthenticationProvider {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication)  {
        String email = authentication.getName();
        String rawPassword = String.valueOf(authentication.getCredentials());

        Account account = accountRepository.findByEmail(email)
                .filter(found -> passwordEncoder.matches(rawPassword, found.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        LoginPrincipal principal = LoginPrincipal.account(account.getId());
        return UsernamePasswordAuthenticationToken.authenticated(principal, null,
                principal.authorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

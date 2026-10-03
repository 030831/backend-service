package com.hyunjun.backend.account.security;


import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.account.repository.AccountRepository;
import com.hyunjun.backend.account.service.AccountService;
import com.hyunjun.backend.common.security.LoginPrincipal;
import com.hyunjun.backend.common.util.EmailNormalizer;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component
public class AccountAuthenticationProvider implements AuthenticationProvider {

    private static final String BAD_CREDENTIALS = "이메일 또는 비밀번호가 올바르지 않습니다.";

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    /** 없는 이메일 때도 bcrypt 비교를 한 번 하려고 앱 시작 때 만들어 두는 해시 **/
    private final String dummyPasswordHash;

    public AccountAuthenticationProvider(AccountRepository accountRepository, AccountService accountService, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Override
    public Authentication authenticate(Authentication authentication)  {
        String email = EmailNormalizer.normalize(authentication.getName());
        String rawPassword = String.valueOf(authentication.getCredentials());
        Instant now = Instant.now();

        Account account = accountRepository.findByEmail(email).orElse(null);

        if (account == null) {
            passwordEncoder.matches(rawPassword, dummyPasswordHash); // 결과는 버리고 시간만 맞춘다.
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        if (account.isLoginLocked(now)) {
            throw new LockedException("로그인 시도가 너무 많습니다. 잠시 후 다시 시도하세요.");
        }

        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            accountService.recordLoginFailure(account.getId(), now);
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        accountService.recordLoginSuccess(account.getId());

        LoginPrincipal principal = LoginPrincipal.account(account.getId());

        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.authorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

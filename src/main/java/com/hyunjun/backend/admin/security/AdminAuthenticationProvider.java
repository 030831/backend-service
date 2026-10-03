package com.hyunjun.backend.admin.security;

import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.admin.domain.AdminAccount;
import com.hyunjun.backend.admin.repository.AdminAccountRepository;
import com.hyunjun.backend.admin.service.AdminAccountService;
import com.hyunjun.backend.common.security.LoginPrincipal;
import com.hyunjun.backend.common.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * POST /auth/login {"email":" Kim@Example.com ","password":"…"}
 * ① 컨트롤러 @Valid (빈 값만 거른다)
 * ② AccountAuthenticationProvider.authenticate
 *    1. email = EmailNormalizer.normalize(입력)          → "kim@example.com"          (D)
 *    2. findByEmail
 *       없으면: 더미 해시와 matches 한 번 → 401            (B, 시간 맞추기)
 *    3. account.isLoginLocked(now)면 → LockedException → 429                        (C)
 *    4. matches(입력 비밀번호, 저장 해시)
 *       틀리면: accountService.recordLoginFailure(id, now) → 401                    (C)
 *               └ 트랜잭션: 행 잠금 조회 → account.recordLoginFailure(now) → 커밋(UPDATE)
 *    5. 맞으면: accountService.recordLoginSuccess(id) (실패 기록이 있을 때만 0으로) (C)
 * ③ SessionLogin.login → 204
 */
@Component
public class AdminAuthenticationProvider implements AuthenticationProvider {

    private static final String BAD_CREDENTIALS = "이메일 또는 비밀번호가 올바르지 않습니다.";

    private final AdminAccountRepository adminAccountRepository;
    private final AdminAccountService adminAccountService;
    private final PasswordEncoder passwordEncoder;
    private final String dummyPasswordHash;

    public AdminAuthenticationProvider(AdminAccountRepository adminAccountRepository, AdminAccountService adminAccountService, PasswordEncoder passwordEncoder) {
        this.adminAccountRepository = adminAccountRepository;
        this.adminAccountService = adminAccountService;
        this.passwordEncoder = passwordEncoder;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String email = EmailNormalizer.normalize(authentication.getName());
        String rawPassword = String.valueOf(authentication.getCredentials());
        Instant now = Instant.now();
        AdminAccount adminAccount = adminAccountRepository.findByEmail(email).orElse(null);

        if (adminAccount == null) {
            passwordEncoder.matches(rawPassword, dummyPasswordHash); // 결과는 버리고 시간만 맞춘다.
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        if (adminAccount.isLoginLocked(now)) {
            throw new LockedException("로그인 시도가 너무 많습니다. 잠시 후 다시 시도하세요.");
        }


        if (!passwordEncoder.matches(rawPassword, adminAccount.getPasswordHash())) {
            adminAccountService.recordLoginFailure(adminAccount.getId(), now);
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        adminAccountService.recordLoginSuccess(adminAccount.getId());

        LoginPrincipal principal = LoginPrincipal.admin(adminAccount.getId());

        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.authorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

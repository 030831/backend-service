package com.hyunjun.backend.admin.security;

import com.hyunjun.backend.admin.domain.AdminAccount;
import com.hyunjun.backend.admin.repository.AdminAccountRepository;
import com.hyunjun.backend.admin.service.AdminAccountService;
import com.hyunjun.backend.common.security.LoginPrincipal;
import com.hyunjun.backend.common.security.PasswordLoginProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class AdminAuthenticationProvider extends PasswordLoginProvider {

    private final AdminAccountRepository adminAccountRepository;
    private final AdminAccountService adminAccountService;

    public AdminAuthenticationProvider(AdminAccountRepository adminAccountRepository, AdminAccountService adminAccountService, PasswordEncoder passwordEncoder) {
        super(passwordEncoder);
        this.adminAccountRepository = adminAccountRepository;
        this.adminAccountService = adminAccountService;
    }

    @Override
    protected Optional<AdminAccount> findByEmail(String email) {
        return adminAccountRepository.findByEmail(email);
    }

    @Override
    protected void recordFailure(Long id, Instant now) {
        adminAccountService.recordLoginFailure(id, now);
    }

    @Override
    protected void recordSuccess(Long id) {
        adminAccountService.recordLoginSuccess(id);
    }

    @Override
    protected LoginPrincipal principalOf(Long id) {
        return LoginPrincipal.admin(id);
    }
}

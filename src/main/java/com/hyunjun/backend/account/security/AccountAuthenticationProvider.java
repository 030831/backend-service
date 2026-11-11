package com.hyunjun.backend.account.security;


import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.account.repository.AccountRepository;
import com.hyunjun.backend.account.service.AccountService;
import com.hyunjun.backend.common.security.LoginPrincipal;
import com.hyunjun.backend.common.security.PasswordLoginProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;


@Component
public class AccountAuthenticationProvider extends PasswordLoginProvider {

    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public AccountAuthenticationProvider(AccountRepository accountRepository, AccountService accountService, PasswordEncoder passwordEncoder) {
        super(passwordEncoder);
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    @Override
    protected Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    protected void recordFailure(Long id, Instant now) {
        accountService.recordLoginFailure(id, now);
    }

    @Override
    protected void recordSuccess(Long id) {
        accountService.recordLoginSuccess(id);
    }

    @Override
    protected LoginPrincipal principalOf(Long id) {
        return LoginPrincipal.account(id);
    }
}

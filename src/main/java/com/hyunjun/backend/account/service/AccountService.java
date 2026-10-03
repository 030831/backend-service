package com.hyunjun.backend.account.service;

import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.account.dto.AccountResponse;
import com.hyunjun.backend.account.exception.AccountNotFoundException;
import com.hyunjun.backend.account.exception.DuplicateAccountException;
import com.hyunjun.backend.account.repository.AccountRepository;
import com.hyunjun.backend.common.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long register(String email, String nickname, String rawPassword) {

        String normalizedEmail = EmailNormalizer.normalize(email);

        if (accountRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateAccountException("이미 사용 중인 이메일입니다.");
        }

        if (accountRepository.existsByNickname(nickname)) {
            throw new DuplicateAccountException("이미 사용 중인 닉네임입니다.");
        }

        Account account = new Account(normalizedEmail, nickname, passwordEncoder.encode(rawPassword));

        try {
            accountRepository.saveAndFlush(account);
        } catch (DataIntegrityViolationException exception) {
            throw translate(exception);
        }

        return account.getId();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계정을 찾을 수 없습니다."));

        return AccountResponse.from(account);
    }

    @Transactional
    public void recordLoginFailure(Long accountId, Instant now) {
        accountRepository.findByIdForUpdate(accountId)
                .ifPresent(account -> account.recordLoginFailure(now));
    }

    @Transactional
    public void recordLoginSuccess(Long accountId) {
        accountRepository.findById(accountId)
                .filter(Account::hasLoginFailures)
                .ifPresent(Account::resetLoginFailures);
    }

    private DuplicateAccountException translate(DataIntegrityViolationException exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();

                if (constraintName != null && constraintName.contains("uk_accounts_email")) {
                    return new DuplicateAccountException("이미 사용 중인 이메일입니다.", exception);
                }

                if (constraintName != null && constraintName.contains("uk_accounts_nickname")) {
                    return new DuplicateAccountException("이미 사용 중인 닉네임입니다.", exception);
                }
            }

            cause = cause.getCause();
        }

        throw exception;
    }
}

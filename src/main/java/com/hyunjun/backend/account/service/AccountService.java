package com.hyunjun.backend.account.service;

import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.account.exception.DuplicateAccountException;
import com.hyunjun.backend.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long register(String email, String nickname, String rawPassword) {
        if (accountRepository.existsByEmail(email)) {
            throw new DuplicateAccountException("이미 사용 중인 이메일입니다.");
        }

        if (accountRepository.existsByNickname(nickname)) {
            throw new DuplicateAccountException("이미 사용 중인 닉네임입니다.");
        }

        Account account = new Account(email, nickname, passwordEncoder.encode(rawPassword));

        try {
            accountRepository.saveAndFlush(account);
        } catch (DataIntegrityViolationException exception) {
            throw translate(exception);
        }

        return account.getId();
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

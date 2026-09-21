package com.hyunjun.backend.account.service;

import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.account.exception.DuplicateAccountException;
import com.hyunjun.backend.account.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long register(String email, String nickname, String password) {

        validate(email, "이메일은 필수 입니다.");
        validate(nickname, "닉네임은 필수 입니다.");
        validate(password, "비밀번호는 필수 입니다.");

        if (accountRepository.existsByEmail(email)) {
            throw new DuplicateAccountException("이미 사용 중인 이메일 입니다.");
        }

        if (accountRepository.existsByNickname(nickname)) {
            throw new DuplicateAccountException("이미 사용 중인 닉네임 입니다.");
        }

        String passwordHash = passwordEncoder.encode(password);

        Account account = new Account(email, nickname, passwordHash);

        try {
            accountRepository.saveAndFlush(account);
        } catch (DataIntegrityViolationException exception) {
            throw translateConstraint(exception);
        }
        return account.getId();
    }

    private RuntimeException translateConstraint(
            DataIntegrityViolationException exception
    ) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                String name = normalizeConstrainName(
                        violation.getConstraintName()
                );

                if ("uk_accounts_email".equals(name)) {
                    return new DuplicateAccountException(
                            "이미 사용 중인 이메일 입니다.", exception
                    );
                }

                if ("uk_accounts_nickname".equals(name)) {
                    return new DuplicateAccountException(
                            "이미 사용 중인 닉네임 입니다.", exception
                    );
                }
            }
            cause = cause.getCause();
        }
        return exception;
    }

    private String normalizeConstrainName(String name) {
        if (name == null) {
            return "";
        }

        String normalized = name.replace("'", "")
                .replace("\"", "")
                .replace("`", "");

        return normalized.substring(normalized.lastIndexOf('.') + 1);
    }

    private void validate(String str, String message) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}

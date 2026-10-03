package com.hyunjun.backend.admin.service;

import com.hyunjun.backend.admin.domain.AdminAccount;
import com.hyunjun.backend.admin.dto.AdminResponse;
import com.hyunjun.backend.admin.exception.AdminAccountNotFoundException;
import com.hyunjun.backend.admin.repository.AdminAccountRepository;
import com.hyunjun.backend.common.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AdminResponse getAdmin(Long adminId) {
        AdminAccount adminAccount = adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new AdminAccountNotFoundException("관리자를 찾을 수 없습니다."));

        return AdminResponse.from(adminAccount);
    }

    @Transactional
    public boolean createIfNoneExists(String email, String name, String rawPassword) {

        String normalizedEmail = EmailNormalizer.normalize(email);

        if (adminAccountRepository.count() > 0) {
            return false;
        }

        adminAccountRepository.saveAndFlush(
                new AdminAccount(normalizedEmail, name, passwordEncoder.encode(rawPassword))
        );

        return true;
    }

    @Transactional
    public void recordLoginFailure(Long accountId, Instant now) {
        adminAccountRepository.findByIdForUpdate(accountId)
                .ifPresent(account -> account.recordLoginFailure(now));
    }

    @Transactional
    public void recordLoginSuccess(Long accountId) {
        adminAccountRepository.findById(accountId)
                .filter(AdminAccount::hasLoginFailures)
                .ifPresent(AdminAccount::resetLoginFailures);
    }
}

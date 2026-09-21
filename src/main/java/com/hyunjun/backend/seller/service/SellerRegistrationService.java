package com.hyunjun.backend.seller.service;

import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.account.repository.AccountRepository;
import com.hyunjun.backend.common.exception.CommerceConflictException;
import com.hyunjun.backend.common.exception.CommerceNotFoundException;
import com.hyunjun.backend.seller.domain.RegistrationStatus;
import com.hyunjun.backend.seller.domain.SellerRegistration;
import com.hyunjun.backend.seller.repository.SellerRegistrationRepository;
import com.hyunjun.backend.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerRegistrationService {

    private final AccountRepository accountRepository;
    private final SellerRepository sellerRepository;
    private final SellerRegistrationRepository registrationRepository;

    @Transactional
    public Long submit(Long accountId, String storeName) {
        Account account = accountRepository.findForUpdate(accountId)
                .orElseThrow(() ->
                        new CommerceNotFoundException("계정이 없습니다."));

        if (sellerRepository.existsByAccount_Id(accountId)) {
            throw new CommerceConflictException("이미 스토어가 있습니다.");
        }

        if (registrationRepository.existsByApplicant_IdAndStatus(
                accountId, RegistrationStatus.SUBMITTED
        )) {
            throw new CommerceConflictException("심사 중인 신청이 있습니다.");
        }

        SellerRegistration registration = new SellerRegistration(account, storeName);

        registrationRepository.save(registration);

        return registration.getId();
    }
}

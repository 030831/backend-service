package com.hyunjun.backend.store.service;

import com.hyunjun.backend.account.exception.DuplicateAccountException;
import com.hyunjun.backend.store.dto.StoreApplicationResponse;
import com.hyunjun.backend.store.domain.StoreApplication;
import com.hyunjun.backend.store.exception.DuplicateStoreApplicationException;
import com.hyunjun.backend.store.repository.StoreApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreApplicationService {

    private final StoreApplicationRepository storeApplicationRepository;

    @Transactional
    public StoreApplicationResponse submit(Long accountId, String storeName) {

        StoreApplication storeApplication = new StoreApplication(accountId, storeName);

        try {
            storeApplicationRepository.saveAndFlush(storeApplication);
        } catch (DataIntegrityViolationException exception) {
            throw translate(exception);
        }

        return StoreApplicationResponse.from(storeApplication);
    }

    private DuplicateStoreApplicationException translate(DataIntegrityViolationException exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();

                if (constraintName != null && constraintName.contains("uk_store_applications_pending")) {
                    return new DuplicateStoreApplicationException("이미 심사 중인 신청이 있습니다.");
                }
            }
            cause = cause.getCause();
        }
        throw exception;
    }

    @Transactional(readOnly = true)
    public List<StoreApplicationResponse> findMyApplications(Long accountId) {
        return storeApplicationRepository.findAllByApplicantAccountIdOrderByIdDesc(accountId)
                .stream()
                .map(StoreApplicationResponse::from)
                .toList();
    }

}

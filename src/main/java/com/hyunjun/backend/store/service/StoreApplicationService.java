package com.hyunjun.backend.store.service;

import com.hyunjun.backend.common.exception.ConstraintViolations;
import com.hyunjun.backend.store.domain.ApplicationStatus;
import com.hyunjun.backend.store.domain.Store;
import com.hyunjun.backend.store.domain.StoreApplication;
import com.hyunjun.backend.store.dto.StoreApplicationResponse;
import com.hyunjun.backend.store.dto.StoreResponse;
import com.hyunjun.backend.store.exception.AlreadyHasStoreException;
import com.hyunjun.backend.store.exception.DuplicateStoreApplicationException;
import com.hyunjun.backend.store.exception.StoreApplicationNotFoundException;
import com.hyunjun.backend.store.repository.StoreApplicationRepository;
import com.hyunjun.backend.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreApplicationService {

    private final StoreApplicationRepository storeApplicationRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public StoreApplicationResponse submit(Long accountId, String storeName) {

        StoreApplication storeApplication = new StoreApplication(accountId, storeName);

        if (storeRepository.existsByAccountId(accountId)) {
            throw new AlreadyHasStoreException("이미 스토어가 있습니다.");
        }

        try {
            storeApplicationRepository.saveAndFlush(storeApplication);
        } catch (DataIntegrityViolationException exception) {
            throw translate(exception);
        }



        return StoreApplicationResponse.from(storeApplication);
    }

    private DuplicateStoreApplicationException translate(DataIntegrityViolationException exception) {
        if (ConstraintViolations.isViolated(exception, "uk_store_applications_pending")) {
            return new DuplicateStoreApplicationException("이미 심사 중인 신청이 있습니다.");
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

    @Transactional
    public StoreResponse approve(Long applicationId, Long adminId) {
        StoreApplication application = storeApplicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new StoreApplicationNotFoundException("신청을 찾을 수 없습니다."));

        Store store = application.approve(adminId, Instant.now());
        storeRepository.save(store);

        return StoreResponse.from(store);
    }

    @Transactional
    public StoreApplicationResponse reject(Long applicationId, Long adminId, String reason) {

        StoreApplication application = storeApplicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new StoreApplicationNotFoundException("신청을 찾을 수 없습니다."));

        application.reject(adminId, Instant.now(), reason);

        return StoreApplicationResponse.from(application);
    }

    @Transactional(readOnly = true)
    public List<StoreApplicationResponse> findSubmitted() {
        return storeApplicationRepository.findAllByStatusOrderByIdAsc(ApplicationStatus.SUBMITTED)
                .stream()
                .map(StoreApplicationResponse::from)
                .toList();
    }
}

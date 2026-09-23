package com.hyunjun.backend.store.service;

import com.hyunjun.backend.store.domain.Store;
import com.hyunjun.backend.store.dto.StoreResponse;
import com.hyunjun.backend.store.exception.StoreNotFoundException;
import com.hyunjun.backend.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public StoreResponse getMyStore(Long accountId) {
        Store store = storeRepository.findByAccountId(accountId)
                .orElseThrow(() -> new StoreNotFoundException("스토어가 없습니다."));

        return StoreResponse.from(store);
    }
}

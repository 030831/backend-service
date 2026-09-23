package com.hyunjun.backend.store.dto;

import com.hyunjun.backend.store.domain.ApplicationStatus;
import com.hyunjun.backend.store.domain.StoreApplication;

import java.time.Instant;

public record StoreApplicationResponse(
        Long id, String storeName, ApplicationStatus status, Instant createdAt
) {
    public static StoreApplicationResponse from(StoreApplication storeApplication) {
        return new StoreApplicationResponse(
                storeApplication.getId(), storeApplication.getStoreName(), storeApplication.getStatus(),
                storeApplication.getCreatedAt()
        );
    }
}

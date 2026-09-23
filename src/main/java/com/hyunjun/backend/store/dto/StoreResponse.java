package com.hyunjun.backend.store.dto;

import com.hyunjun.backend.store.domain.Store;

import java.time.Instant;

public record StoreResponse(
        Long id, String name, Instant createdAt
) {

    public static StoreResponse from(Store store) {
        return new StoreResponse(store.getId(), store.getName(), store.getCreatedAt());
    }
}

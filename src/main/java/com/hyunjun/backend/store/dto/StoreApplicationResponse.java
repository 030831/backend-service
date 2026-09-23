package com.hyunjun.backend.store.dto;

import com.hyunjun.backend.store.domain.ApplicationStatus;
import com.hyunjun.backend.store.domain.StoreApplication;

import java.time.Instant;

public record StoreApplicationResponse(
        Long id, Long applicantAccountId, String storeName, ApplicationStatus status,
        String rejectionReason, Instant createdAt, Instant reviewedAt
) {
    public static StoreApplicationResponse from(StoreApplication storeApplication) {
        return new StoreApplicationResponse(
            storeApplication.getId(), storeApplication.getApplicantAccountId(), storeApplication.getStoreName(),
                storeApplication.getStatus(), storeApplication.getRejectionReason(), storeApplication.getCreatedAt(),
                storeApplication.getReviewedAt()
        );
    }
}

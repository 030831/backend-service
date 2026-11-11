package com.hyunjun.backend.store.dto;

import com.hyunjun.backend.store.domain.StoreApplication;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequest(
        @NotBlank
        @Size(max = StoreApplication.REJECTION_REASON_MAX_LENGTH)
        String reason
) {
}

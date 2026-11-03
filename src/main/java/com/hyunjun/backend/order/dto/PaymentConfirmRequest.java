package com.hyunjun.backend.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PaymentConfirmRequest(
        @NotNull
        @PositiveOrZero
        Long amount
) {
}

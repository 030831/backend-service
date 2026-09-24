package com.hyunjun.backend.product.dto;

import jakarta.validation.constraints.NotNull;

public record StockAdjustRequest(
        @NotNull
        Integer delta
) {
}

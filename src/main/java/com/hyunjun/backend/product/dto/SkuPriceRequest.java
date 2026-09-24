package com.hyunjun.backend.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SkuPriceRequest(
        @NotNull
        @Positive
        Long price
) {
}

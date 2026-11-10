package com.hyunjun.backend.product.dto;

import com.hyunjun.backend.product.domain.Sku;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SkuPriceRequest(
        @NotNull
        @Positive
        @Max(Sku.MAX_PRICE)
        Long price
) {
}

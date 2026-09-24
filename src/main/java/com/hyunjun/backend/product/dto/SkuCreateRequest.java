package com.hyunjun.backend.product.dto;

import com.hyunjun.backend.product.domain.Sku;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SkuCreateRequest(
        @NotNull
        @Size(max = Sku.OPTION_LABEL_MAX_LENGTH)
        String optionLabel,

        @NotNull
        @Positive
        Long price,

        @NotNull
        @Min(0)
        Integer quantity
) {
}

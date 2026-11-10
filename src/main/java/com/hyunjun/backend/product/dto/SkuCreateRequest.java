package com.hyunjun.backend.product.dto;

import com.hyunjun.backend.product.domain.Sku;
import com.hyunjun.backend.product.domain.Stock;
import jakarta.validation.constraints.*;

public record SkuCreateRequest(
        @NotNull
        @Size(max = Sku.OPTION_LABEL_MAX_LENGTH)
        String optionLabel,

        @NotNull
        @Positive
        @Max(Sku.MAX_PRICE)
        Long price,

        @NotNull
        @Min(0)
        @Max(Stock.MAX_QUANTITY)
        Integer quantity
) {
}

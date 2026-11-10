package com.hyunjun.backend.product.dto;

import com.hyunjun.backend.product.domain.Stock;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockAdjustRequest(
        @NotNull
        @Min(-Stock.MAX_QUANTITY)
        @Max(Stock.MAX_QUANTITY)
        Integer delta
) {
}

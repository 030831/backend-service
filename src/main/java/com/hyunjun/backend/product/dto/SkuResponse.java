package com.hyunjun.backend.product.dto;

import com.hyunjun.backend.product.domain.Sku;

public record SkuResponse(
        Long id, String optionLabel, long price, boolean soldOut
) {

    public static SkuResponse from(Sku sku, int quantity) {
        return new SkuResponse(
                sku.getId(), sku.getOptionLabel(), sku.getPrice(),
                quantity == 0
        );
    }
}

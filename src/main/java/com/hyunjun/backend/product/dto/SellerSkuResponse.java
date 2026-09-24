package com.hyunjun.backend.product.dto;

import com.hyunjun.backend.product.domain.Sku;

public record SellerSkuResponse(
        Long id, String optionLabel, long price, int quantity
) {

    public static SellerSkuResponse from(Sku sku, int quantity) {
        return new SellerSkuResponse(sku.getId(), sku.getOptionLabel(), sku.getPrice(), quantity);
    }
}

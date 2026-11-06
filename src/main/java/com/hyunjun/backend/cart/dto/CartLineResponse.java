package com.hyunjun.backend.cart.dto;

import com.hyunjun.backend.product.domain.ProductStatus;

public record CartLineResponse(
        Long cartItemId,
        Long skuId,
        String productName,
        String optionLabel,
        long unitPrice,
        int quantity,
        boolean soldOut,
        boolean onSale
) {
    public static CartLineResponse from(CartLineRow row) {
        return new CartLineResponse(row.cartItemId(), row.skuId(), row.productName(),
                row.optionLabel(), row.unitPrice(), row.quantity(), row.stockQuantity() == 0,
                row.productStatus() == ProductStatus.ON_SALE);
    }
}

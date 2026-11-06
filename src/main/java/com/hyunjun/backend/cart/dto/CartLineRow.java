package com.hyunjun.backend.cart.dto;

import com.hyunjun.backend.product.domain.ProductStatus;

public record CartLineRow(
        Long cartItemId,
        Long skuId,
        String productName,
        String optionLabel,
        Long unitPrice,
        Integer quantity,
        Integer stockQuantity,
        ProductStatus productStatus
) {
}

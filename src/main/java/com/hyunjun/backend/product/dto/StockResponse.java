package com.hyunjun.backend.product.dto;

public record StockResponse(
        Long skuId, int quantity
) {
}

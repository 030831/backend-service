package com.hyunjun.backend.product.dto;

public record ProductSummaryRow(
        Long id, String name, Long lowestPrice, Long totalQuantity
) {
}

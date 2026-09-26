package com.hyunjun.backend.product.dto;

public record ProductSummaryResponse(
        Long id, String name, long lowestPrice, boolean soldOut
) {

    public static ProductSummaryResponse from(ProductSummaryRow row) {
        return new ProductSummaryResponse(
                row.id(), row.name(), row.lowestPrice(), row.totalQuantity() == 0
        );
    }
}

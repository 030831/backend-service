package com.hyunjun.backend.order.dto;

import com.hyunjun.backend.order.domain.OrderLine;
import com.hyunjun.backend.order.domain.OrderLineStatus;

import java.time.Instant;

public record OrderLineResponse(
        Long id,
        Long skuId,
        Long productId,
        Long storeId,
        String productName,
        String optionLabel,
        long unitPrice,
        int quantity,
        long lineAmount,
        OrderLineStatus status,
        Instant canceledAt
) {

    public static OrderLineResponse from(OrderLine line) {
        return new OrderLineResponse(
                line.getId(),
                line.getSkuId(),
                line.getProductId(),
                line.getStoreId(),
                line.getProductName(),
                line.getOptionLabel(),
                line.getUnitPrice(),
                line.getQuantity(),
                line.lineAmount(),
                line.getStatus(),
                line.getCanceledAt());
    }
}

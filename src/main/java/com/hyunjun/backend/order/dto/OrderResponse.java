package com.hyunjun.backend.order.dto;

import com.hyunjun.backend.order.domain.Order;
import com.hyunjun.backend.order.domain.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        long totalAmount,
        long payAmount,
        long refundedAmount,
        Instant expiresAt,
        Instant paidAt,
        Instant canceledAt,
        DeliveryResponse delivery,
        List<OrderLineResponse> lines,
        Instant createdAt
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getPayAmount(),
                order.getRefundedAmount(),
                order.getExpiresAt(),
                order.getPaidAt(),
                order.getCanceledAt(),
                DeliveryResponse.from(order.getDelivery()),
                order.getLines().stream().map(OrderLineResponse::from).toList(),
                order.getCreatedAt()
        );
    }
}

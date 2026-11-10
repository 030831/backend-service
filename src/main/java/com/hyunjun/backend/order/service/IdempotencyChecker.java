package com.hyunjun.backend.order.service;

import com.hyunjun.backend.order.dto.*;
import com.hyunjun.backend.order.exception.IdempotencyKeyReusedException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class IdempotencyChecker {

    public void requireSameOrder(OrderResponse existing, OrderCreateRequest request) {
        requireSameDeliveryAndTotal(existing, request.delivery(), request.expectedTotal());

        Map<Long, Integer> orderQuantities = existing.lines().stream()
                .collect(Collectors.toMap(OrderLineResponse::skuId, OrderLineResponse::quantity));

        Map<Long, Integer> requestedQuantities = request.lines().stream()
                .collect(Collectors.toMap(OrderLineRequest::skuId, OrderLineRequest::quantity));

        checkIdempotency(orderQuantities.equals(requestedQuantities));
    }

    public void requireSameDeliveryAndTotal(OrderResponse existing,
                                            DeliveryRequest delivery,
                                            long expectedTotal) {

        checkIdempotency(existing.totalAmount() == expectedTotal);

        DeliveryResponse requestedDelivery = DeliveryResponse.from(delivery.toDeliveryInfo());

        checkIdempotency(existing.delivery().equals(requestedDelivery));
    }

    public void requireOrderedSkus(OrderResponse existing, List<Long> skuIds) {
        List<Long> orderedSkuIds = existing.lines().stream().map(OrderLineResponse::skuId)
                .toList();
        checkIdempotency(orderedSkuIds.containsAll(skuIds));
    }

    private void checkIdempotency(boolean same) {
        if (!same) {
            throw new IdempotencyKeyReusedException("같은 멱등키로 다른 내용의 주문을 보냈습니다.");
        }
    }
}

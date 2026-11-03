package com.hyunjun.backend.order.service;

import com.hyunjun.backend.order.domain.Order;
import com.hyunjun.backend.order.domain.StockRelease;
import com.hyunjun.backend.order.dto.OrderResponse;
import com.hyunjun.backend.order.exception.OrderNotFoundException;
import com.hyunjun.backend.order.repository.OrderRepository;
import com.hyunjun.backend.product.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderStateService {

    private final OrderRepository orderRepository;
    private final StockService stockService;

    @Transactional
    public OrderResponse confirmPayment(Long orderId, Long accountId, long amount, Instant now) {
        Order order = getMyOrderForUpdate(orderId, accountId);
        order.confirmPayment(amount, now);

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancel(Long orderId, Long accountId, List<Long> lineIds, Instant now) {
        Order order = getMyOrderForUpdate(orderId, accountId);
        List<StockRelease> releases = order.cancel(lineIds, now);

        for (StockRelease release : StockRelease.sortedBySkuId(releases)) {
            stockService.restore(release.skuId(), release.quantity());
        }

        return OrderResponse.from(order);
    }

    private Order getMyOrderForUpdate(Long orderId, Long accountId) {
        return orderRepository.findByIdAndAccountIdForUpdate(orderId, accountId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
    }
}

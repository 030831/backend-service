package com.hyunjun.backend.order.service;

import com.hyunjun.backend.order.domain.Order;
import com.hyunjun.backend.order.domain.OrderStatus;
import com.hyunjun.backend.order.domain.StockRelease;
import com.hyunjun.backend.order.exception.OrderNotFoundException;
import com.hyunjun.backend.order.repository.OrderRepository;
import com.hyunjun.backend.product.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderExpiryService {

    private final OrderRepository orderRepository;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public List<Long> findExpiredIds(Instant now, int limit) {
        return orderRepository.findExpiredIds(OrderStatus.PENDING_PAYMENT, now, PageRequest.of(0, limit));
    }

    /**
     * 주문 하나를 만료한다. 이미 결제/취소된 건이면 아무것도 하지않고 false 를 돌려준다.
     */

    @Transactional
    public boolean expire(Long orderId, Instant now) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        List<StockRelease> releases = order.expire(now);

        for (StockRelease release : StockRelease.sortedBySkuId(releases)) {
            stockService.restore(release.skuId(), release.quantity());
        }

        return !releases.isEmpty();
    }
}

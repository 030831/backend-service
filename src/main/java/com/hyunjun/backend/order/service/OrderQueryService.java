package com.hyunjun.backend.order.service;

import com.hyunjun.backend.order.dto.OrderResponse;
import com.hyunjun.backend.order.exception.OrderNotFoundException;
import com.hyunjun.backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Optional<OrderResponse> findByIdempotencyKey(Long accountId, String idempotencyKey) {
        return orderRepository.findByAccountIdAndIdempotencyKey(accountId, idempotencyKey)
                .map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(Long orderId, Long accountId) {
        return orderRepository.findWithLinesByIdAndAccountId(orderId, accountId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findMyOrders(Long accountId) {
        return orderRepository.findAllWithLinesByAccountId(accountId)
                .stream().map(OrderResponse::from)
                .toList();
    }
}

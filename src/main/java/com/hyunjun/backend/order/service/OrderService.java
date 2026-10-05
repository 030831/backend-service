package com.hyunjun.backend.order.service;

import com.hyunjun.backend.order.dto.OrderCreateRequest;
import com.hyunjun.backend.order.dto.OrderResponse;
import com.hyunjun.backend.order.exception.DuplicateOrderRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderCreator orderCreator;
    private final OrderQueryService orderQueryService;

    /**
     * 트랜잭션을 열지 않는다. 멱등키 조회와 생성 트랜잭션을 순서대로 부르기만 한다.
     */
    public OrderResponse place(Long accountId, OrderCreateRequest request, Instant now) {
        Optional<OrderResponse> existing = orderQueryService.findByIdempotencyKey(accountId, request.idempotencyKey());

        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            return orderCreator.create(accountId, request, now);
        } catch (DuplicateOrderRequestException exception) {
            // 생성 트랜잭션은 롤백됐다. 그 트랜잭션이 끝난 지금, 먼저 저장된 주문을 다시 읽는다.
            return orderQueryService.findByIdempotencyKey(accountId, request.idempotencyKey())
                    .orElseThrow(() -> exception);
        }
    }
}



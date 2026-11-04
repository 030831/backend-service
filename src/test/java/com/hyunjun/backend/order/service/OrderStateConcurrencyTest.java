package com.hyunjun.backend.order.service;

import com.hyunjun.backend.order.dto.OrderLineRequest;
import com.hyunjun.backend.order.dto.OrderResponse;
import com.hyunjun.backend.order.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

class OrderStateConcurrencyTest extends OrderConcurrencyTestSupport {

    @Autowired
    OrderStateService orderStateService;

    @Autowired
    OrderExpiryService orderExpiryService;

    /**
     * 티셔츠 2개 + 양말 1개(총 23,000)을 주문한다. 양말 재고는 1이라 주문 뒤 0 이다.
     */
    private OrderResponse placeBoth(Instant now) {
        return orderService.place(buyerId, request(UUID.randomUUID().toString(), 23000,
                new OrderLineRequest(tshirtSkuId, 2), new OrderLineRequest(socksSkuId, 1)), now);
    }

    private String orderStatus(Long orderId) {
        return jdbcTemplate.queryForObject("SELECT status FROM orders WHERE id = ?", String.class, orderId);
    }

    @Test
    void 결제한_주문의_취소를_동시에_두_번_누르면_재고는_한_번만_돌아온다() throws Exception {
        // given
        // 결제까지 끝난 주문. 티셔츠 재고 98, 양말 재고 0.
        Instant now = Instant.now();
        OrderResponse order = placeBoth(now);
        orderStateService.confirmPayment(order.id(), buyerId, 23000, now);

        // 두 작업이 서비스 호출 전에 만난다. 실행 순서는 고려하지 않는다.
        CyclicBarrier barrier = new CyclicBarrier(2);

        // when
        // [전체 취소] 를 두 번
        List<Object> results = runTogether(
                () -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    return orderStateService.cancel(order.id(), buyerId, List.of(), now);
                },
                () -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    return orderStateService.cancel(order.id(), buyerId, List.of(), now);
                }
        );

        // then
        // 성공 응답 하나, 상태 예외 하나, 재고는 주문 전 값
        assertThat(results).filteredOn(result -> result instanceof OrderResponse).hasSize(1);
        assertThat(results).filteredOn(result -> result instanceof InvalidOrderStateException).hasSize(1);
        assertThat(orderStatus(order.id())).isEqualTo("CANCELED");
        assertThat(stock(tshirtSkuId)).isEqualTo(100);
        assertThat(stock(socksSkuId)).isEqualTo(1);
    }

    @Test
    void 결제_확정과_만료가_겹치면_둘_중_하나만_성공한다() throws Exception {
        // given
        Instant now = Instant.now();
        OrderResponse order = placeBoth(now);

        Instant later = now.plus(Duration.ofMinutes(31));
        CyclicBarrier barrier = new CyclicBarrier(2);

        // when
        List<Object> results = runTogether(
                () -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    return orderStateService.confirmPayment(order.id(), buyerId, 23000, now);
                },
                () -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    return orderExpiryService.expire(order.id(), later);
                }
        );
        // then
        String status = orderStatus(order.id());

        if ("PAID".equals(status)) {
            // 확정이 먼저 된 경우 재고는 돌아오지 않음, 즉, 남은 재고는 0
            assertThat(results).contains(Boolean.FALSE);
            assertThat(stock(socksSkuId)).isEqualTo(0);
        } else if ("EXPIRED".equals(status)) {
            // 만료 먼저일 경우 재고는 돌아오고 예외가 반환됨
            assertThat(results).contains(Boolean.TRUE);
            assertThat(results).filteredOn(result -> result instanceof InvalidOrderStateException).hasSize(1);
            assertThat(stock(socksSkuId)).isEqualTo(1);
        } else {
            // 확정도 아니고 만료도 아닌 상태는 잘못된 결과
            fail("예상하지 못한 주문 상태: " + status);
        }
    }

    @Test
    void 취소와_만료가_겹쳐도_재고는_한_번만_돌아온다() throws Exception {
        // given
        Instant now = Instant.now();
        OrderResponse order = placeBoth(now);

        Instant later = now.plus(Duration.ofMinutes(31));
        CyclicBarrier barrier = new CyclicBarrier(2);

        // when
        List<Object> results = runTogether(
                () -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    return orderStateService.cancel(order.id(), buyerId, List.of(), now);
                },
                () -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    return orderExpiryService.expire(order.id(), later);
                }
        );

        // then
        String status = orderStatus(order.id());

        if ("CANCELED".equals(status)) {
            // 취소가 먼저
            assertThat(results).contains(Boolean.FALSE);
            assertThat(stock(socksSkuId)).isEqualTo(1);
            assertThat(stock(tshirtSkuId)).isEqualTo(100);
        } else if ("EXPIRED".equals(status)) {
            // 만료가 먼저
            assertThat(results).contains(Boolean.TRUE);
            assertThat(results).filteredOn(result -> result instanceof InvalidOrderStateException).hasSize(1);
            assertThat(stock(socksSkuId)).isEqualTo(1);
            assertThat(stock(tshirtSkuId)).isEqualTo(100);
        } else {
            // 둘 다 아닌 경우
            fail("예상하지 못한 주문 상태: " + status);
        }
    }

}

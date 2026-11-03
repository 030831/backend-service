package com.hyunjun.backend.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class OrderExpiryJob {

    static final int BATCH_SIZE = 100;

    private final OrderExpiryService orderExpiryService;

    @Scheduled(fixedDelayString = "${order.expiry.fixed-delay-ms:60000}")
    public void run() {
        Instant now = Instant.now();

        List<Long> orderIds = orderExpiryService.findExpiredIds(now, BATCH_SIZE);

        int expired = 0;

        for (Long orderId : orderIds) {
            try {
                if (orderExpiryService.expire(orderId, now)) {
                    expired++;
                }
            } catch (RuntimeException exception) {
                log.warn("주문 만료 처리에 실패했습니다. orderId = {}", orderId, exception);
            }
        }

        if (!orderIds.isEmpty()) {
            log.info("미결제 주문 만료: 후보 {} 건, 만료 {} 건", orderIds.size(), expired);
        }
    }
}

package com.hyunjun.backend.order.integration;

import com.hyunjun.backend.order.service.OrderExpiryService;
import com.hyunjun.backend.support.TestBrowser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OrderPaymentIntegrationTest extends OrderIntegrationTestSupport{

    @Autowired
    OrderExpiryService orderExpiryService;

    private TestBrowser seller;
    private TestBrowser buyer;
    private Long skuIdA;
    private Long skuIdB;

    @BeforeEach
    void 판매자와_구매자와_티셔츠를_준비한다() {
        seller = openStore("seller@example.com", "철수상점");
        buyer = loggedInCustomer("buyer@example.com", "영희");
        skuIdA = registerSku(seller, "면 티셔츠", 10000, 5);
        skuIdB = registerSku(seller, "양말", 3000, 5);

    }

    /**
     * 새 멱등키로 준비하고 201을 확인한 뒤 본문을 돌려준다.
     */
    private JsonNode placeOrder(TestBrowser buyer, long expectedTotal, Map<?, ?>... lines) {
        HttpResponse<String> response = buyer.post("/orders", orderJson(UUID.randomUUID().toString(), expectedTotal, lines));
        assertThat(response.statusCode()).isEqualTo(201);

        return body(response);
    }


    private HttpResponse<String> pay(TestBrowser buyer, long orderId, long amount) {
        return buyer.post("/orders/" + orderId + "/payment", json(Map.of("amount", amount)));
    }

    /**
     * 전체 취소는 List.of() 를 넘긴다.
     */
    private HttpResponse<String> cancel(TestBrowser buyer, long orderId, List<Long> lineIds) {
        return buyer.post("/orders/" + orderId + "/cancel", json(Map.of("lineIds", lineIds)));
    }

    /**
     * 응답의 lines 중 그 판매 단위의 항복 반환
     */
    private JsonNode lineOf(JsonNode order, long skuId) {
        JsonNode lines = order.get("lines");

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).get("skuId").asLong() == skuId) {
                return lines.get(i);
            }
        }

        throw new AssertionError("skuId " + skuId + " 항목이 없습니다 : " + order);
    }

    private long placeTshirtAndSocksOrder() {
        return placeOrder(buyer, 16000, line(skuIdA, 1), line(skuIdB, 2)).get("id").asLong();
    }

    @Test
    void 맞는_금액으로_확정하면_PAID이고_틀린_금액과_재확정_409이다() {
        // given
        // 재고 5인 티셔츠(10,000원) 2개를 주문한 구매자. 재고3
        long orderId = placeOrder(buyer, 20000, line(skuIdA, 2)).get("id").asLong();

        // when
        // 틀린 금액 -> 맞는 금액 -> 다시 확정
        HttpResponse<String> wrongAmount = pay(buyer, orderId, 19999);
        HttpResponse<String> paid = pay(buyer, orderId, 20000);
        HttpResponse<String> paidAgain = pay(buyer, orderId, 20000);

        // then
        // 409 -> 결제 금액이 다릅니다.
        // 200 -> PAID,paidAt 기록
        // 409 -> 이미 결제된 주문입니다.
        assertThat(wrongAmount.statusCode()).isEqualTo(409);
        assertThat(body(wrongAmount).get("detail").asString()).isEqualTo("결제 금액이 다릅니다.");

        assertThat(paid.statusCode()).isEqualTo(200);
        assertThat(body(paid).get("status").asString()).isEqualTo("PAID");
        assertThat(body(paid).get("paidAt").isNull()).isFalse();

        assertThat(paidAgain.statusCode()).isEqualTo(409);
        assertThat(body(paidAgain).get("detail").asString()).isEqualTo("이미 결제된 주문입니다.");

        assertThat(stock(skuIdA)).isEqualTo(3);
    }

    @Test
    void 결제_전_항목_하나를_취소하면_payAmount가_줄고_옛_금액_확정은_409_새_금액은_200이다() {
        // given

        long orderId = placeTshirtAndSocksOrder();
        JsonNode order = body(buyer.get("/orders/" + orderId));
        long lineIdB = lineOf(order, skuIdB).get("id").asLong();

        // when
        HttpResponse<String> response = cancel(buyer, orderId, List.of(lineIdB));

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(body(response).get("status").asString()).isEqualTo("PENDING_PAYMENT");
        assertThat(body(response).get("payAmount").asLong()).isEqualTo(10000);
        assertThat(body(response).get("totalAmount").asLong()).isEqualTo(16000);
        assertThat(lineOf(body(response), skuIdB).get("status").asString()).isEqualTo("CANCELED");
        assertThat(lineOf(body(response), skuIdB).get("canceledAt").isNull()).isEqualTo(false);
        assertThat(lineOf(body(response), skuIdA).get("status").asString()).isEqualTo("ORDERED");
        assertThat(stock(skuIdA)).isEqualTo(4);
        assertThat(stock(skuIdB)).isEqualTo(5);
    }

    @Test
    void 전체_취소하면_CANCELED이_재고가_모두_돌아오며_그_뒤_확정과_취소는_409이다() {
        // given
        long orderId = placeTshirtAndSocksOrder();
        JsonNode order = body(buyer.get("/orders/" + orderId));

        // when
        long lineIdA = lineOf(order, skuIdA).get("id").asLong();
        long lineIdB = lineOf(order, skuIdB).get("id").asLong();

        HttpResponse<String> response = cancel(buyer, orderId, List.of(lineIdA, lineIdB));

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(body(response).get("status").asString()).isEqualTo("CANCELED");
        assertThat(body(response).get("canceledAt").isNull()).isEqualTo(false);
        assertThat(body(response).get("payAmount").asLong()).isEqualTo(0);
        assertThat(lineOf(body(response), skuIdA).get("status").asString()).isEqualTo("CANCELED");
        assertThat(lineOf(body(response), skuIdB).get("status").asString()).isEqualTo("CANCELED");
        assertThat(stock(skuIdA)).isEqualTo(5);
        assertThat(stock(skuIdB)).isEqualTo(5);
    }

    @Test
    void 결제_뒤_항목_취소는_refundedAmount에_더하고_주문은_PAID로_남는다() {
        // given
        long orderId = placeTshirtAndSocksOrder();

        HttpResponse<String> paid = pay(buyer, orderId, 16000);
        assertThat(paid.statusCode()).isEqualTo(200);

        JsonNode order = body(buyer.get("/orders/" + orderId));
        long lineIdA = lineOf(order, skuIdA).get("id").asLong();
        long lineIdB = lineOf(order, skuIdB).get("id").asLong();

        // when
        HttpResponse<String> response = cancel(buyer, orderId, List.of(lineIdB));

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(body(response).get("status").asString()).isEqualTo("PAID");
        assertThat(body(response).get("payAmount").asLong()).isEqualTo(16000);
        assertThat(body(response).get("refundedAmount").asLong()).isEqualTo(6000);
        assertThat(stock(skuIdA)).isEqualTo(4);
        assertThat(stock(skuIdB)).isEqualTo(5);

        // when
        HttpResponse<String> rest = cancel(buyer, orderId, List.of(lineIdA));
        assertThat(rest.statusCode()).isEqualTo(200);
        assertThat(body(rest).get("status").asString()).isEqualTo("CANCELED");
        assertThat(body(rest).get("refundedAmount").asLong()).isEqualTo(16000);
        assertThat(stock(skuIdA)).isEqualTo(5);
        assertThat(stock(skuIdB)).isEqualTo(5);
    }

    @Test
    void 이미_취소된_항목은_409이고_이_주문의_항목이_아니면_404이며_반쪽_취소는_없다() {
        // given
        long orderId = placeTshirtAndSocksOrder();

        JsonNode order = body(buyer.get("/orders/" + orderId));
        long lineIdA = lineOf(order, skuIdA).get("id").asLong();
        long lineIdB = lineOf(order, skuIdB).get("id").asLong();

        HttpResponse<String> response = cancel(buyer, orderId, List.of(lineIdB));
        assertThat(response.statusCode()).isEqualTo(200);

        // when
        HttpResponse<String> again = cancel(buyer, orderId, List.of(lineIdB));
        assertThat(again.statusCode()).isEqualTo(409);
        assertThat(body(again).get("detail").asString()).isEqualTo("이미 취소된 항목입니다.");

        HttpResponse<String> unknown = cancel(buyer, orderId, List.of(999999L));
        assertThat(unknown.statusCode()).isEqualTo(404);
        assertThat(body(unknown).get("detail").asString()).isEqualTo("주문 항목을 찾을 수 없습니다.");

        HttpResponse<String> mixed = cancel(buyer, orderId, List.of(lineIdA, 999999L));
        assertThat(mixed.statusCode()).isEqualTo(404);

        // then
        order = body(buyer.get("/orders/" + orderId));
        assertThat(lineOf(order, skuIdA).get("status").asString()).isEqualTo("ORDERED");
        assertThat(stock(skuIdA)).isEqualTo(4);
    }

    @Test
    void 남의_주문의_확정과_취소는_404다() {
        // given
        long orderId = placeOrder(buyer, 20000, line(skuIdA, 2)).get("id").asLong();
        TestBrowser other = loggedInCustomer("other@example.com", "민수");

        // when
        HttpResponse<String> paid = pay(other, orderId, 20000);
        HttpResponse<String> canceled = cancel(other, orderId, List.of());

        // then
        assertThat(paid.statusCode()).isEqualTo(404);
        assertThat(canceled.statusCode()).isEqualTo(404);

        assertThat(body(buyer.get("/orders/" + orderId)).get("status").asString()).isEqualTo("PENDING_PAYMENT");
        assertThat(stock(skuIdA)).isEqualTo(3);
    }

    @Test
    void 기한이_지난_주문은_작업_전_확정이_409이고_작업은_한_번만_재고를_돌려준다() {
        // given
        long orderIdP = placeOrder(buyer, 20000, line(skuIdA, 2)).get("id").asLong();
        long orderIdQ = placeOrder(buyer, 10000, line(skuIdA, 1)).get("id").asLong();

        jdbcTemplate.update("UPDATE orders SET expires_at = UTC_TIMESTAMP(6) - INTERVAL 1 MINUTE WHERE id = ?", orderIdP);

        // when
        HttpResponse<String> beforeJob = pay(buyer, orderIdP, 20000);
        List<Long> ids = orderExpiryService.findExpiredIds(Instant.now(), 100);

        boolean first = orderExpiryService.expire(orderIdP, Instant.now());
        boolean second = orderExpiryService.expire(orderIdP, Instant.now());
        JsonNode expired = body(buyer.get("/orders/" + orderIdP));
        JsonNode order = body(buyer.get("/orders/" + orderIdQ));

        // then
        assertThat(beforeJob.statusCode()).isEqualTo(409);
        assertThat(body(beforeJob).get("detail").asString()).isEqualTo("결제 기한이 지났습니다.");
        assertThat(ids).containsExactly(orderIdP);
        assertThat(first).isTrue();
        assertThat(second).isFalse();
        assertThat(expired.get("status").asString()).isEqualTo("EXPIRED");
        assertThat(order.get("status").asString()).isEqualTo("PENDING_PAYMENT");
        assertThat(stock(skuIdA)).isEqualTo(4);

        // when
        HttpResponse<String> afterJob = pay(buyer, orderIdP, 20000);
        assertThat(afterJob.statusCode()).isEqualTo(409);
        assertThat(body(afterJob).get("detail").asString()).isEqualTo("만료된 주문입니다.");

    }
}

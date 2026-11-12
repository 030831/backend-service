package com.hyunjun.backend.common;

import com.hyunjun.backend.order.integration.OrderIntegrationTestSupport;
import com.hyunjun.backend.support.TestBrowser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class InputGuardIntegrationTest extends OrderIntegrationTestSupport {

    private TestBrowser seller;
    private TestBrowser buyer;
    private Long skuId;

    @BeforeEach
    void 판매자와_구매자와_상품을_준비한다() {
        seller = openStore("seller@example.com", "철수상점");
        buyer = loggedInCustomer("buyer@example.com", "영희");
        skuId = registerSku(seller, "면 티셔츠", 10000, 5);
    }

    @Test
    void 주문_항목에_null이_있으면_400이다() {
        // when
        HttpResponse<String> response = buyer.post("/orders", json(Map.of(
                "lines", Arrays.asList((Object) null),
                "idempotencyKey", "k",
                "expectedTotal", 0,
                "delivery", delivery("영희")
        )));

        // then
        assertBadRequest(response);
        assertThat(orderCount()).isZero();
    }

    @Test
    void 상품의_판매_단위에_null이_있으면_400이다() {
        // when
        HttpResponse<String> response = seller.post("/seller/products", json(Map.of(
                "name", "모자",
                "description", "설명",
                "skus", Arrays.asList((Object) null)
        )));

        // then
        assertBadRequest(response);
    }

    @Test
    void 취소할_항목_번호에_null이_있으면_400이고_재고는_그대로다() {
        // given
        long orderId = placeOrder(buyer, 10000, line(skuId, 1));

        // when
        HttpResponse<String> response = buyer.post("/orders/" + orderId + "/cancel", json(Map.of(
                "lineIds", Arrays.asList((Object) null)
        )));

        // then
        assertBadRequest(response);
        assertThat(stock(skuId)).isEqualTo(4);
    }

    @Test
    void 가격이_1억을_넘으면_400이고_1억은_등록된다() {
        // when
        HttpResponse<String> firstResponse = registerProduct(100000000L, 1);
        HttpResponse<String> secondResponse = registerProduct(100000001L, 1);

        // then
        assertThat(firstResponse.statusCode()).isEqualTo(201);
        assertBadRequest(secondResponse);
    }

    @Test
    void 재고_증감량이_상한을_넘으면_400이고_재고는_그대로다() {
        // when
        HttpResponse<String> firstResponse = adjustStock(1000000);
        HttpResponse<String> secondResponse = adjustStock(Integer.MAX_VALUE);

        // then
        assertThat(firstResponse.statusCode()).isEqualTo(200);
        assertBadRequest(secondResponse);
        assertThat(stock(skuId)).isEqualTo(1000000 + 5);
    }

    @Test
    void 주문_항목이_100개를_넘으면_400이다() {
        // when
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 101; i++) {
            list.add(line((long) i, 1));
        }

        HttpResponse<String> response = buyer.post("/orders", json(Map.of(
                "lines", list,
                "idempotencyKey", "k",
                "expectedTotal", 0,
                "delivery", delivery("영희"))));

        // then
        assertBadRequest(response);
        assertThat(body(response).get("errors").size()).isEqualTo(1);
        assertThat(body(response).get("errors").get(0).get("field").asString()).isEqualTo("lines");
    }

    @Test
    void 재고_행이_없으면_404이다() {
        // given
        // update 메서드는 반환타입이 필요 없기 때문에 Long.class를 넣을 필요가 없음
        jdbcTemplate.update(
                "DELETE FROM stocks WHERE sku_id = ?", skuId
        );

        // when
        HttpResponse<String> response = adjustStock(1);

        // then
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(detail(response)).isEqualTo("재고 행을 찾을 수 없습니다.");
    }

    @Test
    void 재고가_모자라게_빼면_409이다() {
        // when
        // 기존 재고 수는 5
        HttpResponse<String> response = adjustStock(-6);

        // then
        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(detail(response)).isEqualTo("재고가 부족합니다.");
    }



    private void assertBadRequest(HttpResponse<String> response) {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(detail(response)).isEqualTo("입력값이 올바르지 않습니다.");
    }

    private HttpResponse<String> registerProduct(long price, int quantity) {
        return seller.post("/seller/products", json(Map.of(
                "name", "모자",
                "description", "설명",
                "skus", List.of(Map.of("optionLabel", "", "price", price, "quantity", quantity))
        )));
    }

    private HttpResponse<String> adjustStock(int delta) {
        return seller.patch("/seller/products/" + productIdOf(skuId) + "/skus/" + skuId + "/stock",
                json(Map.of(
                        "delta", delta
                )));
    }
}

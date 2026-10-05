package com.hyunjun.backend.order.integration;

import com.hyunjun.backend.support.IntegrationTestSupport;
import com.hyunjun.backend.support.TestBrowser;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OrderIntegrationTest extends IntegrationTestSupport {

    /**
     * 가입, 로그인한 판매자가 스토어를 신청하고 관리자가 승인한다. 판매자 브라우저를 돌려준다.
     */
    private TestBrowser openStore(String email, String storeName) {
        TestBrowser seller = loggedInCustomer(email, "판매자");

        HttpResponse<String> applied = seller.post("/store-applications", json(Map.of("storeName", storeName)));

        assertThat(applied.statusCode()).isEqualTo(201);
        long applicationId = body(applied).get("id").asLong();
        assertThat(loggedInAdmin().post("/admin/store-applications/" + applicationId + "/approve",
                "{}").statusCode()).isEqualTo(200);

        return seller;
    }

    /**
     * 판매 단위 하나짜리 상품을 등록하고 그 판매 단위의 id(skuId)를 돌려준다.
     */
    private Long registerSku(TestBrowser seller, String name, long price, int quantity) {
        HttpResponse<String> response = seller.post("/seller/products", json(Map.of(
                "name", name,
                "description", "설명",
                "skus", List.of(Map.of("optionLabel", "", "price", price, "quantity", quantity))
        )));

        assertThat(response.statusCode()).isEqualTo(201);

        return body(response).get("skus").get(0).get("id").asLong();
    }

    /**
     * 주문 요청 본문, 항목은 line(skuId, 수량) 으로 만든다.
     */
    private String orderJson(String key, long expectedTotal, Map<?, ?>... lines) {
        return json(Map.of(
                "idempotencyKey", key,
                "lines", List.of(lines),
                "delivery", Map.of(
                        "recipientName", "영희",
                        "recipientPhone", "010-1234-5678",
                        "zipCode", "04524",
                        "address1", "서울 중구 세종대로 110"
                ),
                "expectedTotal", expectedTotal
        ));
    }

    private Map<String, Object> line(Long skuId, int quantity) {
        return Map.of("skuId", skuId, "quantity", quantity);
    }

    /**
     * 재고는 화면이 아니라 DB에서 바로 읽는다.
     */
    private int stock(Long skuId) {
        return jdbcTemplate.queryForObject("SELECT quantity FROM stocks WHERE sku_id = ?",
                Integer.class, skuId);
    }

    private int orderCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
    }

    @Test
    void 주문하면_201과_PENDING_PAYMENT를_받고_재고가_줄어든다() {
        // given
        // 재고가 5인 티셔츠 (10,000원)와 로그인한 구매자
        TestBrowser seller = openStore("seller@example.com", "철수상점");
        Long skuId = registerSku(seller, "면 티셔츠", 10000, 5);
        TestBrowser buyer = loggedInCustomer("buyer@example.com", "영희");

        // when
        // 2개를 주문한다.
        HttpResponse<String> response = buyer.post("/orders", orderJson("key-1", 20000, line(skuId, 2)));

        // then
        // 결제 대기 주문이 생기고 재고가 2 줄었다.
        assertThat(response.statusCode()).isEqualTo(201);
        JsonNode order = body(response);
        assertThat(order.get("status").asString()).isEqualTo("PENDING_PAYMENT");
        assertThat(order.get("totalAmount").asLong()).isEqualTo(20000);
        assertThat(order.get("lines")).hasSize(1);
        assertThat(stock(skuId)).isEqualTo(3);
    }

    @Test
    void 같은_멱등키로_다시_보내면_같은_주문_ID를_받고_재고는_한_번만_줄어든다() {
        // given
        TestBrowser seller = openStore("seller@example.com", "철수상점");
        Long skuId = registerSku(seller, "면 티셔츠", 10000, 5);
        TestBrowser buyer = loggedInCustomer("buyer@example.com", "영희");

        // when
        HttpResponse<String> firstResponse = buyer.post("/orders", orderJson("key-1", 20000, line(skuId, 2)));
        HttpResponse<String> secondResponse = buyer.post("/orders", orderJson("key-1", 20000, line(skuId, 2)));

        // then
        assertThat(firstResponse.statusCode()).isEqualTo(201);
        assertThat(secondResponse.statusCode()).isEqualTo(201);
        assertThat(body(secondResponse).get("id").asLong()).isEqualTo(body(firstResponse).get("id").asLong());
        assertThat(stock(skuId)).isEqualTo(3);
        assertThat(orderCount()).isEqualTo(1);
    }

    @Test
    void 화면_총액이_다르면_409이고_재고와_주문은_그대로다() {
        // given
        TestBrowser seller = openStore("seller@example.com", "철수상점");
        Long skuId = registerSku(seller, "면 티셔츠", 10000, 5);
        TestBrowser buyer = loggedInCustomer("buyer@example.com", "영희");

        // when
        HttpResponse<String> response = buyer.post("/orders", orderJson("key-1", 19999, line(skuId, 2)));

        // then
        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(body(response).get("detail").asString()).isEqualTo("가격이 바뀌었습니다. 주문서를 다시 확인해 주세요.");
        assertThat(stock(skuId)).isEqualTo(5);
        assertThat(orderCount()).isEqualTo(0);
    }

    @Test
    void 재고가_모자라면_409이고_다른_항목의_재고와_주문_행도_되돌아간다() {
        // given
        TestBrowser seller = openStore("seller@example.com", "철수상점");
        Long skuId1 = registerSku(seller, "면 티셔츠", 10000, 5);
        Long skuId2 = registerSku(seller, "양말", 3000, 1);
        TestBrowser buyer = loggedInCustomer("buyer@example.com", "영희");

        // when
        HttpResponse<String> response = buyer.post("/orders", orderJson("key-1", 16000, line(skuId1, 1), line(skuId2, 2)));

        // then
        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(body(response).get("detail").asString()).isEqualTo("재고가 부족합니다: 양말");
        assertThat(stock(skuId1)).isEqualTo(5);
        assertThat(stock(skuId2)).isEqualTo(1);
        assertThat(orderCount()).isEqualTo(0);
    }

    @Test
    void 남의_주문은_404이고_내_목록에는_내_주문만_있다() {
        // given
        TestBrowser seller = openStore("seller@example.com", "철수상점");
        Long skuId = registerSku(seller, "면 티셔츠", 10000, 5);
        TestBrowser buyer = loggedInCustomer("buyer@example.com", "영희");

        HttpResponse<String> buyerResponse = buyer.post("/orders", orderJson("key-1", 20000, line(skuId, 2)));
        long id = body(buyerResponse).get("id").asLong();

        TestBrowser other = loggedInCustomer("other@example.com", "민수");

        // then
        assertThat(buyer.get("/orders/" + id).statusCode()).isEqualTo(200);
        assertThat(other.get("/orders/" + id).statusCode()).isEqualTo(404);
        assertThat(body(other.get("/orders/" + id)).get("detail").asString()).isEqualTo("주문을 찾을 수 없습니다.");
        assertThat(buyer.get("/orders/me").statusCode()).isEqualTo(200);

        HttpResponse<String> response = buyer.get("/orders/me");
        assertThat(body(response)).hasSize(1);
        assertThat(body(response).get(0).get("id").asLong()).isEqualTo(id);

    }
}

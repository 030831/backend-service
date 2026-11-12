package com.hyunjun.backend.order.integration;

import com.hyunjun.backend.support.IntegrationTestSupport;
import com.hyunjun.backend.support.TestBrowser;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class OrderIntegrationTestSupport extends IntegrationTestSupport {

    /**
     * 가입, 로그인한 판매자가 스토어를 신청하고 관리자가 승인한다. 판매자 브라우저를 돌려준다.
     */
    protected TestBrowser openStore(String email, String storeName) {
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
    protected Long registerSku(TestBrowser seller, String name, long price, int quantity) {
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
    protected String orderJson(String key, long expectedTotal, Map<?, ?>... lines) {
        return orderJsonTo("영희", key, expectedTotal, lines);
    }

    protected String orderJsonTo(String recipientName, String key, long expectedTotal, Map<?, ?>... lines) {
        return json(Map.of(
                "idempotencyKey", key,
                "lines", List.of(lines),
                "delivery", delivery(recipientName),
                "expectedTotal", expectedTotal
        ));
    }

    protected Map<String, String> delivery(String recipientName) {
        return Map.of(
                "recipientName", recipientName,
                "recipientPhone", "010-1234-5678",
                "zipCode", "04524",
                "address1", "서울 중구 세종대로 110"
        );
    }

    /**
     * 새 멱등키로 준비하고 201을 확인한 뒤 본문을 돌려준다.
     */
    protected long placeOrder(TestBrowser buyer, long expectedTotal, Map<?, ?>... lines) {
        String key = UUID.randomUUID().toString();

        String orderJson = orderJson(key, expectedTotal, lines);

        HttpResponse<String> response = buyer.post("/orders", orderJson);
        assertThat(response.statusCode()).isEqualTo(201);

        return body(response).get("id").asLong();
    }

    protected Long productIdOf(Long skuId) {
        return jdbcTemplate.queryForObject(
                "SELECT product_id FROM skus WHERE id = ?",
                Long.class, skuId
        );
    }

    protected JsonNode elementWithSkuId(JsonNode array, long skuId) {
        for (JsonNode jsonNode : array) {
            if (jsonNode.get("skuId").asLong() == skuId) {
                return jsonNode;
            }
        }

        throw new AssertionError("skuId " + skuId + " 원소가 없습니다. " + array);
    }

    protected Map<String, Object> line(Long skuId, int quantity) {
        return Map.of("skuId", skuId, "quantity", quantity);
    }
}

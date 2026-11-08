package com.hyunjun.backend.cart.integration;

import com.hyunjun.backend.order.integration.OrderIntegrationTestSupport;
import com.hyunjun.backend.support.TestBrowser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CartIntegrationTest extends OrderIntegrationTestSupport {

    private TestBrowser seller;
    private TestBrowser buyer;
    private Long skuIdA;
    private Long skuIdB;

    @BeforeEach
    void 판매자와_구매자와_상품을_준비한다() {
        seller = openStore("seller@example.com", "철수상점");
        buyer = loggedInCustomer("buyer@example.com", "영희");
        skuIdA = registerSku(seller, "면 티셔츠", 10000, 5);
        skuIdB = registerSku(seller, "양말", 3000, 5);
    }

    @Test
    void 같은_판매_단위를_두_번_담으면_행은_하나이고_수량이_합쳐진다() {
        // given
        // 장바구니가 비어있는 상태

        // when
        HttpResponse<String> firstResponse = addToCart(buyer, skuIdA, 2);
        HttpResponse<String> secondResponse = addToCart(buyer, skuIdA, 3);

        // then
        assertThat(firstResponse.statusCode()).isEqualTo(200);
        assertThat(secondResponse.statusCode()).isEqualTo(200);
        assertThat(body(firstResponse).get("quantity").asLong()).isEqualTo(2);
        assertThat(body(secondResponse).get("quantity").asLong()).isEqualTo(5);
        assertThat(body(firstResponse).get("cartItemId").asLong()).isEqualTo(
                body(secondResponse).get("cartItemId").asLong()
        );
        assertThat(cartItemCount()).isEqualTo(1);
        assertThat(stock(skuIdA)).isEqualTo(5);
    }

    @Test
    void 합친_수량이_100을_넘으면_409이고_수량은_그대로다() {
        // given
        addToCart(buyer, skuIdA, 99);

        // when
        HttpResponse<String> response = addToCart(buyer, skuIdA, 2);

        // then
        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(detail(response)).isEqualTo("장바구니에는 판매 단위마다 100개까지 담을 수 있습니다.");
        assertThat(cartLineOf(myCart(buyer), skuIdA).get("quantity").asLong()).isEqualTo(99);
    }

    @Test
    void 판매_중지된_상품은_담을_수_없다() {
        // given
        stopProductOf(skuIdA);

        // when
        HttpResponse<String> response = addToCart(buyer, skuIdA, 1);

        // then
        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(detail(response)).isEqualTo("판매 중지된 상품입니다: 면 티셔츠");
        assertThat(cartItemCount()).isZero();
    }

    @Test
    void 장바구니_조회는_상품_이름_옵션_단가_수량과_품절_판매중_여부를_보여준다() {
        // given
        Long skuIdC = registerSku(seller, "모자", 5000, 0);

        addToCart(buyer, skuIdA, 2);
        addToCart(buyer, skuIdB, 1);
        addToCart(buyer, skuIdC, 1);
        stopProductOf(skuIdB);

        // when
        JsonNode jsonNode = myCart(buyer);
        JsonNode skuA = cartLineOf(jsonNode, skuIdA);
        JsonNode skuB = cartLineOf(jsonNode, skuIdB);
        JsonNode skuC = cartLineOf(jsonNode, skuIdC);

        // then
        assertThat(jsonNode.size()).isEqualTo(3);

        assertThat(skuA.get("productName").asString()).isEqualTo("면 티셔츠");
        assertThat(skuB.get("productName").asString()).isEqualTo("양말");
        assertThat(skuC.get("productName").asString()).isEqualTo("모자");

        assertThat(skuA.get("optionLabel").asString()).isEqualTo("");
        assertThat(skuB.get("optionLabel").asString()).isEqualTo("");
        assertThat(skuC.get("optionLabel").asString()).isEqualTo("");

        assertThat(skuA.get("unitPrice").asLong()).isEqualTo(10000);
        assertThat(skuB.get("unitPrice").asLong()).isEqualTo(3000);
        assertThat(skuC.get("unitPrice").asLong()).isEqualTo(5000);

        assertThat(skuA.get("quantity").asLong()).isEqualTo(2);
        assertThat(skuB.get("quantity").asLong()).isEqualTo(1);
        assertThat(skuC.get("quantity").asLong()).isEqualTo(1);

        assertThat(skuA.get("soldOut").asBoolean()).isEqualTo(false);
        assertThat(skuB.get("soldOut").asBoolean()).isEqualTo(false);
        assertThat(skuC.get("soldOut").asBoolean()).isEqualTo(true);

        assertThat(skuA.get("onSale").asBoolean()).isEqualTo(true);
        assertThat(skuB.get("onSale").asBoolean()).isEqualTo(false);
        assertThat(skuC.get("onSale").asBoolean()).isEqualTo(true);
    }

    @Test
    void 수량을_바꾸면_그_값이_저장된다() {
        // given
        long cartItemId = putInCart(buyer, skuIdA, 2);

        // when
        HttpResponse<String> response = buyer.patch("/cart/items/" + cartItemId, json(Map.of(
                "quantity", 4
        )));

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(body(response).get("quantity").asLong()).isEqualTo(4);
        assertThat(cartLineOf(myCart(buyer), skuIdA).get("quantity").asLong()).isEqualTo(4);
    }

    @Test
    void 항목을_삭제하면_204이고_장바구니에서_빠진다() {
        // given
        long cartItemId = putInCart(buyer, skuIdA, 2);
        addToCart(buyer, skuIdB, 1);

        // when
        HttpResponse<String> response = buyer.delete("/cart/items/" + cartItemId);

        // then
        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(cartItemCount()).isEqualTo(1);
        assertThat(myCart(buyer).get(0).get("skuId").asLong()).isEqualTo(skuIdB);
    }

    @Test
    void 남의_장바구니_항목은_수정과_삭제_모두_404이다() {
        // given
        long cartItemId = putInCart(buyer, skuIdA, 2);

        TestBrowser other = loggedInCustomer("other@example.com", "민수");

        // when
        HttpResponse<String> firstResponse = other.patch("/cart/items/" + cartItemId, json(Map.of(
                "quantity", 5
        )));

        HttpResponse<String> secondResponse = other.delete("/cart/items/" + cartItemId);

        // then
        assertThat(firstResponse.statusCode()).isEqualTo(404);
        assertThat(secondResponse.statusCode()).isEqualTo(404);

        assertThat(detail(firstResponse)).isEqualTo("장바구니 항목을 찾을 수 없습니다.");
        assertThat(detail(secondResponse)).isEqualTo("장바구니 항목을 찾을 수 없습니다.");
        assertThat(cartLineOf(myCart(buyer), skuIdA).get("quantity").asLong()).isEqualTo(2);
    }

    @Test
    void 고른_항목만_주문되고_그_항목만_장바구니에서_빠진다() {
        // given
        long cartItemId = putInCart(buyer, skuIdA, 1);
        addToCart(buyer, skuIdB, 2);

        // when
        HttpResponse<String> response = orderFromCart(buyer, UUID.randomUUID().toString(), 10000, cartItemId);

        // then
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(body(response).get("status").asString()).isEqualTo("PENDING_PAYMENT");
        assertThat(body(response).get("totalAmount").asLong()).isEqualTo(10000);
        assertThat(body(response).get("lines")).hasSize(1);
        assertThat(body(response).get("lines").get(0).get("skuId").asLong()).isEqualTo(skuIdA);
        assertThat(body(response).get("lines").get(0).get("quantity").asLong()).isEqualTo(1);
        assertThat(stock(skuIdA)).isEqualTo(4);
        assertThat(stock(skuIdB)).isEqualTo(5);
        assertThat(cartItemCount()).isEqualTo(1);
        assertThat(cartLineOf(myCart(buyer), skuIdB).get("quantity").asLong()).isEqualTo(2);
    }

    @Test
    void 같은_멱등키로_다시_보내면_같은_주문을_돌려주고_재고는_한_번만_줄어든다() {
        // given
        long cartItemId = putInCart(buyer, skuIdA, 2);
        String key = UUID.randomUUID().toString();

        // when
        HttpResponse<String> firstResponse = orderFromCart(buyer, key, 20000, cartItemId);
        HttpResponse<String> secondResponse = orderFromCart(buyer, key, 20000, cartItemId);

        // then
        assertThat(firstResponse.statusCode()).isEqualTo(201);
        assertThat(secondResponse.statusCode()).isEqualTo(201);

        assertThat(body(firstResponse).get("id").asLong()).isEqualTo(
                body(secondResponse).get("id").asLong()
        );
        assertThat(orderCount()).isEqualTo(1);
        assertThat(stock(skuIdA)).isEqualTo(3);
        assertThat(cartItemCount()).isZero();
    }

    @Test
    void 재고가_부족한_항목을_골라_주문하면_409이고_장바구니는_그대로다() {
        // given
        long cartItemId = putInCart(buyer, skuIdA, 6);

        // when
        HttpResponse<String> response = orderFromCart(buyer, UUID.randomUUID().toString(), 60000, cartItemId);

        // then
        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(detail(response)).isEqualTo("재고가 부족합니다: 면 티셔츠");
        assertThat(orderCount()).isZero();
        assertThat(stock(skuIdA)).isEqualTo(5);
        assertThat(cartLineOf(myCart(buyer), skuIdA).get("quantity").asLong()).isEqualTo(6);
    }

    @Test
    void 남의_장바구니_항목을_골라_주문하면_404이고_주문은_생기지_않는다() {
        // given
        long cartItemId = putInCart(buyer, skuIdA, 1);

        TestBrowser other = loggedInCustomer("other@example.com", "민수");

        // when
        HttpResponse<String> response = orderFromCart(other, UUID.randomUUID().toString(), 10000, cartItemId);

        // then
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(detail(response)).isEqualTo("장바구니 항목을 찾을 수 없습니다.");
        assertThat(orderCount()).isZero();
        assertThat(stock(skuIdA)).isEqualTo(5);
        assertThat(cartLineOf(myCart(buyer), skuIdA).get("quantity").asLong()).isEqualTo(1);
    }

    @Test
    void 같은_판매_단위를_동시에_두_번_담아도_행은_하나이고_수량은_2다() throws Exception {
        // given
        buyer.get("/auth/me");

        // when
        List<Object> results = runTogether(
                () -> addToCart(buyer, skuIdA, 1),
                () -> addToCart(buyer, skuIdA, 1)
        );

        // then
        assertThat(results).filteredOn(result ->
                result instanceof HttpResponse<?> response && response.statusCode() == 200)
                .hasSize(2);
        assertThat(cartItemCount()).isEqualTo(1);
        assertThat(cartLineOf(myCart(buyer), skuIdA).get("quantity").asLong()).isEqualTo(2);
    }

    private HttpResponse<String> addToCart(TestBrowser browser, Long skuId, int quantity) {
        return browser.post("/cart/items", json(Map.of(
                "skuId", skuId, "quantity", quantity
        )));
    }

    private long putInCart(TestBrowser browser, Long skuId, int quantity) {
        HttpResponse<String> response = addToCart(browser, skuId, quantity);

        assertThat(response.statusCode()).isEqualTo(200);

        return body(response).get("cartItemId").asLong();
    }

    private JsonNode myCart(TestBrowser browser) {
        HttpResponse<String> response = browser.get("/cart");

        assertThat(response.statusCode()).isEqualTo(200);

        return body(response);
    }

    private JsonNode cartLineOf(JsonNode cart, long skuId) {

        for (JsonNode jsonNode : cart) {
            if (jsonNode.get("skuId").asLong() == skuId) {
                return jsonNode;
            }
        }

        throw new AssertionError("skuId " + skuId + " 항목이 장바구니에 없습니다: " + cart);
    }

    private HttpResponse<String> orderFromCart(TestBrowser browser, String key, long expectedTotal, Long... cartItemIds) {
        return browser.post("/cart/order", json(Map.of(
                "idempotencyKey" , key, "cartItemIds", cartItemIds,
                "delivery", Map.of(
                        "recipientName", "영희",
                        "recipientPhone", "010-1234-5678",
                        "zipCode", "04524",
                        "address1", "서울 중구 세종대로 110"
                ),
                "expectedTotal", expectedTotal
        )));
    }

    private void stopProductOf(Long skuId) {
        Long productId = jdbcTemplate.queryForObject("SELECT product_id FROM skus WHERE id = ?", Long.class, skuId);

        HttpResponse<String> response = seller.post("/seller/products/" + productId + "/stop", "{}");

        assertThat(response.statusCode()).isEqualTo(200);
    }

    private int cartItemCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cart_items", Integer.class);
    }

    private String detail(HttpResponse<String> response) {
        return body(response).get("detail").asString();
    }
}

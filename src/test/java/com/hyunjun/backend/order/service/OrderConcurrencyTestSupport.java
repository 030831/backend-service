package com.hyunjun.backend.order.service;

import com.hyunjun.backend.account.service.AccountService;
import com.hyunjun.backend.order.dto.DeliveryRequest;
import com.hyunjun.backend.order.dto.OrderCreateRequest;
import com.hyunjun.backend.order.dto.OrderLineRequest;
import com.hyunjun.backend.product.dto.ProductCreateRequest;
import com.hyunjun.backend.product.dto.SkuCreateRequest;
import com.hyunjun.backend.product.service.SellerProductService;
import com.hyunjun.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

abstract class OrderConcurrencyTestSupport extends IntegrationTestSupport {

    @Autowired
    protected OrderService orderService;

    @Autowired
    AccountService accountService;

    @Autowired
    SellerProductService sellerProductService;

    protected Long buyerId;
    protected Long otherBuyerId;
    protected Long tshirtSkuId; // 10,000원, 재고 100
    protected Long socksSkuId; // 3,000원, 재고 1

    @BeforeEach
    void 판매자와_상품과_구매자를_만든다() {
        // 구매자 둘과 판매자 하나. HTTP없이 서비스로 바로 만든다(빠르고 계정 id를 바로 받는다)
        buyerId = accountService.register("buyer@example.com", "구매자", PASSWORD);
        otherBuyerId = accountService.register("other@example.com", "다른구매자", PASSWORD);
        Long sellerId = accountService.register("seller@example.com", "판매자", PASSWORD);

        // Store 생성자는 승인 경로 전용(package-private) 이라 테스트는 SQL로 스토어를 만든다.
        jdbcTemplate.update(
                "INSERT INTO stores (account_id, name, created_at, updated_at) " +
                        "VALUES (?, ?, NOW(6), NOW(6))",
                sellerId,
                "테스트상점"
        );

        Long storeId = jdbcTemplate.queryForObject(
                "SELECT id FROM stores WHERE account_id = ?", Long.class, sellerId
        );

        // 상품 등록은 3-1 서비스를 그대로 쓴다. 재고 행도 함께 생긴다.
        tshirtSkuId = sellerProductService.register(storeId, new ProductCreateRequest(
                "면 티셔츠", "면 100%", List.of(
                new SkuCreateRequest("흰색 L", 10000L, 100)
        ))).skus().get(0).id();

        socksSkuId = sellerProductService.register(storeId, new ProductCreateRequest(
                "양말 3켤레", "면", List.of(
                new SkuCreateRequest("", 3000L, 1)
        ))).skus().get(0).id();
    }


    protected OrderCreateRequest request(String key, long expectedTotal, OrderLineRequest... lines) {
        DeliveryRequest delivery = new DeliveryRequest("영희", "010-1234-5678", "04524",
                "서울 중구 세종대로 110", null, null);

        return new OrderCreateRequest(key, List.of(lines), delivery, expectedTotal);
    }

    protected int stock(Long skuId) {
        return jdbcTemplate.queryForObject(
                "SELECT quantity FROM stocks WHERE sku_id = ?",
                Integer.class,
                skuId
        );
    }

    protected int orderCount() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders", Integer.class
        );
    }

}

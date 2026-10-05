package com.hyunjun.backend.order.service;

import com.hyunjun.backend.account.service.AccountService;
import com.hyunjun.backend.order.dto.DeliveryRequest;
import com.hyunjun.backend.order.dto.OrderCreateRequest;
import com.hyunjun.backend.order.dto.OrderLineRequest;
import com.hyunjun.backend.order.dto.OrderResponse;
import com.hyunjun.backend.product.dto.ProductCreateRequest;
import com.hyunjun.backend.product.dto.SkuCreateRequest;
import com.hyunjun.backend.product.exception.InsufficientStockException;
import com.hyunjun.backend.product.service.ProductQueryService;
import com.hyunjun.backend.product.service.SellerProductService;
import com.hyunjun.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

class OrderConcurrencyTest extends IntegrationTestSupport {

    @Autowired
    OrderService orderService;

    @Autowired
    AccountService accountService;

    @Autowired
    SellerProductService sellerProductService;

    // spy : 실제 빈을 그대로 쓰되, 특정 메서드 앞뒤에 코드를 끼워 넣는다. 테스트마다 설정이 초기화된다.
    @MockitoSpyBean
    ProductQueryService productQueryService;

    @MockitoSpyBean
    OrderQueryService orderQueryService;

    Long buyerId;
    Long otherBuyerId;
    Long tshirtSkuId; // 10,000원, 재고 100
    Long socksSkuId; // 3,000원, 재고 1

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

    private OrderCreateRequest request(String key, long expectedTotal, OrderLineRequest... lines) {
        DeliveryRequest delivery = new DeliveryRequest("영희", "010-1234-5678", "04524",
                "서울 중구 세종대로 110", null, null);

        return new OrderCreateRequest(key, List.of(lines), delivery, expectedTotal);
    }

    private int stock(Long skuId) {
        return jdbcTemplate.queryForObject(
                "SELECT quantity FROM stocks WHERE sku_id = ?",
                Integer.class,
                skuId
        );
    }

    private int orderCount() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders", Integer.class
        );
    }

    /**
     * 두 작업을 동시에 실행하고 결과(성공 값 또는 예외)를 순서대로 모은다. 20초 안에 안 끝나면 실패(교착 감지).
     */
    private List<Object> runTogether(Callable<?> first, Callable<?> second) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            List<Future<?>> futures = List.of(executor.submit(first), executor.submit(second));
            List<Object> results = new ArrayList<>();

            for (Future<?> future : futures) {
                try {
                    results.add(future.get(20, TimeUnit.SECONDS));
                } catch (ExecutionException exception) {
                    results.add(exception.getCause());
                }
            }

            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void 재고가_1개인_양말을_두_사람이_동시에_주문하면_한_명만_성공한다() throws Exception {
        // given
        // 두 스레드가 모두 판매 단위 조회를 끝낸 뒤에야 INSERT 재고 차감으로 넘어가게 하는 대기 지점
        CyclicBarrier barrier = new CyclicBarrier(2);

        // 실제 조회를 한 뒤, 다른 스레드가 여기 올 때까지 기다린다. 이 순간 두 트랜잭션이 모두 열려있다.
        Mockito.doAnswer(invocation -> {
            Object skus = invocation.callRealMethod();
            barrier.await(10, TimeUnit.SECONDS);
            return skus;
        }).when(productQueryService).findOrderableSkus(Mockito.any());

        Instant now = Instant.now();


        // when
        // 서로 다른 두 구매자가 같은 양말 1개를 동시에 주문한다.
        List<Object> results = runTogether(
                () -> orderService.place(buyerId, request("key-a", 3000, new OrderLineRequest(socksSkuId, 1)), now),
                () -> orderService.place(otherBuyerId, request("key-b", 3000, new OrderLineRequest(socksSkuId, 1)), now));

        // then
        // 성공 하나, 재고 부족 하나, 주문 1건, 재고 0
        assertThat(results).filteredOn(result -> result instanceof OrderResponse).hasSize(1);
        assertThat(results).filteredOn(result -> result instanceof InsufficientStockException).hasSize(1);
        assertThat(orderCount()).isEqualTo(1);
        assertThat(stock(socksSkuId)).isZero();
    }

    @Test
    void 같은_멱등키로_동시에_두_번_주문하면_주문은_하나이고_두_응답의_ID가_같다() throws Exception {
        // given
        // 두 스레드 모두 "기존 주문 없음"을 본 뒤에야 생성으로 넘어가게 한다.
        // UNIQUE 위반 뒤의 재조회(결과 있음)에서는 기다리면 안되므로, 비어있을 때만 베리어에 선다.
        CyclicBarrier barrier = new CyclicBarrier(2);

        Mockito.doAnswer(invocation -> {
            Object result = invocation.callRealMethod();

            if (((Optional<?>) result).isEmpty()) {
                barrier.await(10, TimeUnit.SECONDS);
            }

            return result;
        }).when(orderQueryService).findByIdempotencyKey(Mockito.any(), Mockito.any());

        Instant now = Instant.now();
        OrderCreateRequest sameRequest = request("same-key", 13000,
                new OrderLineRequest(tshirtSkuId, 1), new OrderLineRequest(socksSkuId, 1));

        // when
        // 같은 사람이 같은 본문을 동시에 두 번 보낸다([주문하기] 버튼 두 번 누름)
        List<Object> results = runTogether(
                () -> orderService.place(buyerId, sameRequest, now),
                () -> orderService.place(buyerId, sameRequest, now));

        // then
        // 둘다 성공 응답이고 같은 주문이다. 주문 1건, 재고는 한 번만 줄었다.
        assertThat(results).allMatch(result -> result instanceof OrderResponse,
                "둘 다 성공 응답이어야 한다: " + results);
        assertThat(((OrderResponse) results.get(0)).id()).isEqualTo(((OrderResponse) results.get(1)).id());
        assertThat(orderCount()).isEqualTo(1);
        assertThat(stock(tshirtSkuId)).isEqualTo(99);
        assertThat(stock(socksSkuId)).isZero();
    }

    @Test
    void 두_품목을_반대_순서로_동시에_주문해도_교착_없이_모두_성공한다() throws Exception {
        // given
        // 양말 재고도 넉넉히, 스레드 20개, 짝수는 [티셔츠, 양말], 홀수는 [양말, 티셔츠] 순서로 요청한다.
        jdbcTemplate.update(
                "UPDATE stocks SET quantity = 100 WHERE sku_id = ?", socksSkuId
        );
        int threads = 20;
        CyclicBarrier barrier = new CyclicBarrier(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        Instant now = Instant.now();

        try {
            // when
            // 모두 베리어에서 만난 뒤 동시에 출발한다.
            List<Future<OrderResponse>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                boolean reversed = i % 2 == 1;
                String key = "key-" + i;

                futures.add(executor.submit(() -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    OrderLineRequest tshirt = new OrderLineRequest(tshirtSkuId, 1);
                    OrderLineRequest socks = new OrderLineRequest(socksSkuId, 1);

                    return orderService.place(buyerId, reversed
                            ? request(key, 13000, socks, tshirt)
                            : request(key, 13000, tshirt, socks), now);
                }));
            }

            // 실패한 스레드의 예외를 모은다. 교착이면 CannotAcquireLockException이 여기 들어온다.
            List<Throwable> failures = new ArrayList<>();

            for (Future<OrderResponse> future : futures) {
                try {
                    future.get(30, TimeUnit.SECONDS);
                } catch (ExecutionException exception) {
                    failures.add(exception.getCause());
                }
            }

            // then
            // 실패 0, 주문 20건, 두 재고 모두 정확히 20개 감소.
            assertThat(failures).isEmpty();
            assertThat(orderCount()).isEqualTo(threads);
            assertThat(stock(tshirtSkuId)).isEqualTo(80);
            assertThat(stock(socksSkuId)).isEqualTo(80);

        } finally {
            executor.shutdownNow();
        }
    }
}

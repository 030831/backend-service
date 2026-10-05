package com.hyunjun.backend.order.service;

import com.hyunjun.backend.order.domain.Order;
import com.hyunjun.backend.order.domain.OrderLine;
import com.hyunjun.backend.order.dto.OrderCreateRequest;
import com.hyunjun.backend.order.dto.OrderLineRequest;
import com.hyunjun.backend.order.dto.OrderResponse;
import com.hyunjun.backend.order.exception.DuplicateOrderRequestException;
import com.hyunjun.backend.order.exception.PriceChangedException;
import com.hyunjun.backend.order.repository.OrderRepository;
import com.hyunjun.backend.product.domain.Sku;
import com.hyunjun.backend.product.exception.InsufficientStockException;
import com.hyunjun.backend.product.service.ProductQueryService;
import com.hyunjun.backend.product.service.StockService;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderCreator {

    private final OrderRepository orderRepository;
    private final ProductQueryService productQueryService;
    private final StockService stockService;

    @Transactional
    public OrderResponse create(Long accountId, OrderCreateRequest request, Instant now) {
        // a. 판매 단위 상품 조회 없는 품목은 404, 판매 중지 409는 여기서 끝낸다.
        List<Long> skuIds = request.lines().stream().map(OrderLineRequest::skuId).toList();
        Map<Long, Sku> skuById = productQueryService.findOrderableSkus(skuIds).stream()
                .collect(Collectors.toMap(Sku::getId, Function.identity()));

        // b. 주문 당시 값을 복사해 줄을 만들고, 총액을 화면 총액과 대조한다(재고를 건들이기 전)
        Order order = new Order(accountId, request.delivery().toDeliveryInfo(), request.idempotencyKey(), now);

        for (OrderLineRequest lineRequest : request.lines()) {
            order.addLine(skuById.get(lineRequest.skuId()), lineRequest.quantity());
        }

        if (order.getTotalAmount() != request.expectedTotal()) {
            throw new PriceChangedException("가격이 바뀌었습니다. 주문서를 다시 확인해 주세요.");
        }

        // c. 주문과 줄을 먼저 INSERT 한다. 같은 멱등키의 두 번째 요청은 여기서 끝나 재고를 건들이지 않는다.
        try {
            orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException exception) {
            throw translate(exception);
        }

        // d. sku_id 오름차순으로 재고를 뺀다. 모자라면 예외 -> 앞 줄 차감과 INSERT가 모두 롤백된다.
        List<OrderLine> linesBySkuId = order.getLines().stream().
                sorted(Comparator.comparing(OrderLine::getSkuId))
                .toList();

        for (OrderLine line : linesBySkuId) {
            if (!stockService.deduct(line.getSkuId(), line.getQuantity())) {
                throw new InsufficientStockException("재고가 부족합니다: " + line.getProductName());
            }
        }

        // e. 응답 변환도 트랜잭션 안에서 끝낸다(open-in-view가 꺼져있다)
        return OrderResponse.from(order);
    }

    private RuntimeException translate(DataIntegrityViolationException exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();

                if (constraintName != null && constraintName.contains("uk_orders_idempotency")) {
                    return new DuplicateOrderRequestException("같은 멱등키의 주문이 이미 있습니다.", exception);
                }
            }

            cause = cause.getCause();
        }

        return exception;
    }

}

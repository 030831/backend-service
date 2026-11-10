package com.hyunjun.backend.order.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
import com.hyunjun.backend.order.exception.InvalidOrderStateException;
import com.hyunjun.backend.order.exception.OrderLineNotFoundException;
import com.hyunjun.backend.order.exception.PaymentAmountMismatchException;
import com.hyunjun.backend.product.domain.Sku;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    public static final Duration PAYMENT_TIMEOUT = Duration.ofMinutes(30);
    public static final int IDEMPOTENCY_KEY_MAX_LENGTH = 64;
    public static final int MAX_LINES = 100;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private long totalAmount;

    @Column(nullable = false)
    private long payAmount;

    @Column(nullable = false)
    private long refundedAmount;

    @Embedded
    private DeliveryInfo delivery;

    @Column(nullable = false, length = IDEMPOTENCY_KEY_MAX_LENGTH)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant paidAt;

    private Instant canceledAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderLine> lines = new ArrayList<>();


    public Order(Long accountId, DeliveryInfo delivery, String idempotencyKey, Instant now) {
        if (accountId == null) {
            throw new IllegalArgumentException("계정 아이디가 비어 있습니다.");
        }

        if (delivery == null) {
            throw new IllegalArgumentException("배송지가 비어 있습니다.");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank() ||
                idempotencyKey.length() > IDEMPOTENCY_KEY_MAX_LENGTH) {
            throw new IllegalArgumentException("멱등키는 비어 있지 않은 " + IDEMPOTENCY_KEY_MAX_LENGTH +
                    "자 이하여야 합니다.");
        }

        if (now == null) {
            throw new IllegalArgumentException("주문 시각이 비어 있습니다.");
        }

        this.accountId = accountId;
        this.delivery = delivery;
        this.idempotencyKey = idempotencyKey;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.totalAmount = 0;
        this.payAmount = 0;
        this.refundedAmount = 0;
        this.expiresAt = now.plus(PAYMENT_TIMEOUT);
    }

    public OrderLine addLine(Sku sku, int quantity) {
        boolean duplicated = lines.stream()
                .anyMatch(line -> line.getSkuId().equals(sku.getId()));

        if (duplicated) {
            throw new IllegalArgumentException("같은 판매 단위가 이미 담겨 있습니다: " + sku.getId());
        }

        OrderLine line = new OrderLine(this, sku, quantity);
        lines.add(line);

        this.totalAmount += line.lineAmount();
        this.payAmount = this.totalAmount;

        return line;
    }

    public void confirmPayment(long amount, Instant now) {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStateException(stateMessage());
        }

        if (now.isAfter(expiresAt)) {
            throw new InvalidOrderStateException("결제 기한이 지났습니다.");
        }

        if (amount != payAmount) {
            throw new PaymentAmountMismatchException("결제 금액이 다릅니다.");
        }

        this.status = OrderStatus.PAID;
        this.paidAt = now;
    }

    public List<StockRelease> cancel(List<Long> lineIds, Instant now) {
        requireCancelable();

        List<OrderLine> targets = linesToCancel(lineIds);
        List<StockRelease> releases = cancelLines(targets, now);

        applyCancelAmount(targets);
        closeIfAllCanceled(now);

        return releases;
    }

    public List<StockRelease> expire(Instant now) {
        if (status != OrderStatus.PENDING_PAYMENT || now.isBefore(expiresAt)) {
            return List.of();
        }

        List<StockRelease> releases = cancelLines(orderedLines(), now);

        this.status = OrderStatus.EXPIRED;

        return releases;
    }

    private void requireCancelable() {
        if (status != OrderStatus.PENDING_PAYMENT && status != OrderStatus.PAID) {
            throw new InvalidOrderStateException(stateMessage());
        }
    }

    private List<OrderLine> linesToCancel(List<Long> lineIds) {
        return (lineIds == null || lineIds.isEmpty())
                ? orderedLines()
                : findLines(lineIds);
    }

    private List<StockRelease> cancelLines(List<OrderLine> targets, Instant now) {
        return targets.stream()
                .map(line -> line.cancel(now))
                .toList();
    }

    private void applyCancelAmount(List<OrderLine> targets) {
        if (status == OrderStatus.PENDING_PAYMENT) {
            this.payAmount = orderedLines().stream().mapToLong(OrderLine::lineAmount).sum();
        } else {
            this.refundedAmount += targets.stream()
                    .mapToLong(OrderLine::lineAmount)
                    .sum();
        }
    }

    private void closeIfAllCanceled(Instant now) {
        if (orderedLines().isEmpty()) {
            this.status = OrderStatus.CANCELED;
            this.canceledAt = now;
        }
    }

    private List<OrderLine> orderedLines() {
        return lines.stream().filter(OrderLine::isOrdered).toList();
    }

    private List<OrderLine> findLines(List<Long> lineIds) {
        List<OrderLine> found = new ArrayList<>();

        for (Long lineId : lineIds) {
            OrderLine line = lines.stream()
                    .filter(candidate -> lineId.equals(candidate.getId()))
                    .findFirst()
                    .orElseThrow(() -> new OrderLineNotFoundException("주문 항목을 찾을 수 없습니다."));
            found.add(line);
        }

        return found;
    }

    private String stateMessage() {
        return switch (status) {
            case PAID -> "이미 결제된 주문입니다.";
            case CANCELED -> "취소된 주문입니다.";
            case EXPIRED -> "만료된 주문입니다.";
            case PENDING_PAYMENT -> "결제 대기 중인 주문입니다.";
        };
    }
}

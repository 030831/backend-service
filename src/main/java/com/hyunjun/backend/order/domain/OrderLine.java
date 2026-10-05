package com.hyunjun.backend.order.domain;

import com.hyunjun.backend.product.domain.Product;
import com.hyunjun.backend.product.domain.Sku;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "order_lines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderLine {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false, length = Product.NAME_MAX_LENGTH)
    private String productName;

    @Column(nullable = false, length = Sku.OPTION_LABEL_MAX_LENGTH)
    private String optionLabel;

    @Column(nullable = false)
    private long unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderLineStatus status;

    private Instant canceledAt;

    OrderLine(Order order, Sku sku, int quantity) {
        if (order == null || sku == null) {
            throw new IllegalArgumentException("주문과 판매 단위가 필요합니다.");
        }

        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        Product product = sku.getProduct();

        this.order = order;
        this.skuId = sku.getId();
        this.productId = product.getId();
        this.storeId = product.getStoreId();
        this.productName = product.getName();
        this.optionLabel = sku.getOptionLabel();
        this.unitPrice = sku.getPrice();
        this.quantity = quantity;
        this.status = OrderLineStatus.ORDERED;
    }

    public long lineAmount() {
        return unitPrice * quantity;
    }

    public boolean isOrdered() {
        return status == OrderLineStatus.ORDERED;
    }
}

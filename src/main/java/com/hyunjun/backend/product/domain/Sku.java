package com.hyunjun.backend.product.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Stock Keeping Unit(재고 관리 단위)
 */
@Entity
@Table(name = "skus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sku extends BaseTimeEntity {

    public static final int OPTION_LABEL_MAX_LENGTH = 100;
    public static final long MAX_PRICE = 100_000_000L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = OPTION_LABEL_MAX_LENGTH)
    private String optionLabel;

    @Column(nullable = false)
    private long price;

    Sku(Product product, String optionLabel, long price) {
        if (product == null) {
            throw new IllegalArgumentException("상품이 비어 있습니다.");
        }

        if (optionLabel == null || optionLabel.length() > OPTION_LABEL_MAX_LENGTH) {
            throw new IllegalArgumentException("옵션 라벨은 " + OPTION_LABEL_MAX_LENGTH + "자 이하여야 합니다.");
        }

        this.product = product;
        this.optionLabel = optionLabel;
        changePrice(price);
    }

    public void changePrice(long price) {
        if (price <= 0 || price > MAX_PRICE) {
            throw new IllegalArgumentException("가격은 0보다 크고 " + MAX_PRICE + " 이하여야 합니다.");
        }

        this.price = price;
    }
}

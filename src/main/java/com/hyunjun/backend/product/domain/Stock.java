package com.hyunjun.backend.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private int quantity;

    public Stock(Long skuId, int quantity) {
        if (skuId == null) {
            throw new IllegalArgumentException("판매 단위 아이디가 비어 있습니다.");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("수량은 0 이상이어야 합니다.");
        }

        this.skuId = skuId;
        this.quantity = quantity;
    }
}

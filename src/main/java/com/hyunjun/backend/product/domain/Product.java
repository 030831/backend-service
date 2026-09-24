package com.hyunjun.backend.product.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
import com.hyunjun.backend.product.exception.SkuNotFoundException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    public static final int NAME_MAX_LENGTH = 100;
    public static final int DESCRIPTION_MAX_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Sku> skus = new ArrayList<>();

    public Product(Long storeId, String name, String description) {
        if (storeId == null) {
            throw new IllegalArgumentException("스토어 아이디가 비어 있습니다.");
        }

        this.storeId = storeId;
        this.name = requiredText(name, NAME_MAX_LENGTH, "상품 이름");
        this.description = requiredText(description, DESCRIPTION_MAX_LENGTH, "상품 설명");
        this.status = ProductStatus.ON_SALE;
    }

    public Sku addSku(String optionLabel, long price) {
        boolean duplicated = skus.stream()
                .anyMatch(sku -> sku.getOptionLabel().equals(optionLabel));

        if (duplicated) {
            throw new IllegalArgumentException("같은 옵션 라벨이 이미 있습니다: " + optionLabel);
        }

        Sku sku = new Sku(this, optionLabel, price);
        skus.add(sku);

        return sku;
    }

    public Sku getSku(Long skuId) {
        return skus.stream()
                .filter(sku -> skuId.equals(sku.getId()))
                .findFirst()
                .orElseThrow(() -> new SkuNotFoundException("판매 단위를 찾을 수 없습니다."));
    }

    public void update(String name, String description) {
        this.name = requiredText(name, NAME_MAX_LENGTH, "상품 이름");
        this.description = requiredText(description, DESCRIPTION_MAX_LENGTH, "상품 설명");
    }

    public void stop() {
        this.status = ProductStatus.STOPPED;
    }

    public void resume() {
        this.status = ProductStatus.ON_SALE;
    }

    private static String requiredText(String value, int maxLength, String fieldName) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + "은(는) 비어 있지 않은 " + maxLength + "자 이하여야 합니다."
            );
        }

        return value;
    }
}

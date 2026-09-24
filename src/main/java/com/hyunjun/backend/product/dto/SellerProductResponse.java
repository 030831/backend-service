package com.hyunjun.backend.product.dto;

import com.hyunjun.backend.product.domain.Product;
import com.hyunjun.backend.product.domain.ProductStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SellerProductResponse(
        Long id, String name, String description, ProductStatus status,
        List<SellerSkuResponse> skus, Instant createdAt
) {

    public static SellerProductResponse from(Product product, Map<Long, Integer> quantityBySkuId) {
        return new SellerProductResponse(
                product.getId(), product.getName(), product.getDescription(), product.getStatus(),
                product.getSkus().stream()
                        .map(sku -> SellerSkuResponse.from(sku, quantityBySkuId.get(sku.getId())))
                        .toList(),
                product.getCreatedAt()
        );
    }
}

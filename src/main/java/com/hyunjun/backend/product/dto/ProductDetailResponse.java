package com.hyunjun.backend.product.dto;

import com.hyunjun.backend.product.domain.Product;

import java.util.List;
import java.util.Map;

public record ProductDetailResponse(
        Long id, String name, String description, List<SkuResponse> skus
) {
    public static ProductDetailResponse from(Product product, Map<Long, Integer> quantityBySkuId) {
        return new ProductDetailResponse(
                product.getId(), product.getName(), product.getDescription(),
                product.getSkus().stream()
                        .map(sku -> SkuResponse.from(sku, quantityBySkuId.get(sku.getId())))
                        .toList());
    }
}

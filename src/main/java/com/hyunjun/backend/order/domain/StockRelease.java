package com.hyunjun.backend.order.domain;

import java.util.Comparator;
import java.util.List;

/**
 * 취소, 만료로 재고에 되돌릴 (판매 단위, 수량) 한 쌍
 */
public record StockRelease(Long skuId, int quantity) {

    /**
     * 재고 행은 항상 sku_id 오름차순으로 잠근다. 주문 차감과 같은 순서다.
     */
    public static List<StockRelease> sortedBySkuId(List<StockRelease> releases) {
        return releases.stream()
                .sorted(Comparator.comparing(StockRelease::skuId))
                .toList();
    }
}

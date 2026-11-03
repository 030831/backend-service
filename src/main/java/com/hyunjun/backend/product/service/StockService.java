package com.hyunjun.backend.product.service;

import com.hyunjun.backend.product.domain.Stock;
import com.hyunjun.backend.product.exception.InsufficientStockException;
import com.hyunjun.backend.product.exception.SkuNotFoundException;
import com.hyunjun.backend.product.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional
    public void create(Long skuId, int quantity) {
        stockRepository.save(new Stock(skuId, quantity));
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> findQuantities(List<Long> skuIds) {
        return stockRepository.findAllBySkuIdIn(skuIds)
                .stream()
                .collect(
                        Collectors.toMap(Stock::getSkuId, Stock::getQuantity)
                );
    }

    @Transactional
    public int adjust(Long skuId, int delta) {
        int updatedRows = stockRepository.adjustQuantity(skuId, delta);

        // TODO: 재고 행이 없는 경우에도 0이 반환되므로, 재고 부족과 데이터 누락을 구분해야 한다.
        if (updatedRows == 0) {
            throw new InsufficientStockException("재고가 부족합니다.");
        }

        return stockRepository.findBySkuId(skuId)
                .orElseThrow(() -> new SkuNotFoundException("재고 행을 찾을 수 없습니다."))
                .getQuantity();
    }

    @Transactional
    public boolean deduct(Long skuId, int quantity) {
        requirePositive(quantity);

        return stockRepository.adjustQuantity(skuId, -quantity) == 1;
    }

    @Transactional
    public void restore(Long skuId, int quantity) {
        requirePositive(quantity);

        if (stockRepository.adjustQuantity(skuId, quantity) == 0) {
            throw new IllegalStateException("재고 행이 없습니다. skudId = " + skuId);
        }
    }

    private static void requirePositive(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
    }
}

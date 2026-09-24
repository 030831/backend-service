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

        if (updatedRows == 0) {
            throw new InsufficientStockException("재고가 부족합니다.");
        }

        return stockRepository.findBySkuId(skuId)
                .orElseThrow(() -> new SkuNotFoundException("재고 행을 찾을 수 없습니다."))
                .getQuantity();
    }
}

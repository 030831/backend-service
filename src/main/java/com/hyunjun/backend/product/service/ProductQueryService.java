package com.hyunjun.backend.product.service;

import com.hyunjun.backend.common.dto.PageResponse;
import com.hyunjun.backend.product.domain.Product;
import com.hyunjun.backend.product.domain.ProductStatus;
import com.hyunjun.backend.product.domain.Sku;
import com.hyunjun.backend.product.dto.ProductDetailResponse;
import com.hyunjun.backend.product.dto.ProductSummaryResponse;
import com.hyunjun.backend.product.exception.ProductNotFoundException;
import com.hyunjun.backend.product.exception.ProductNotOnSaleException;
import com.hyunjun.backend.product.exception.SkuNotFoundException;
import com.hyunjun.backend.product.repository.ProductRepository;
import com.hyunjun.backend.product.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final StockService stockService;
    private final SkuRepository skuRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> findOnSale(Pageable pageable) {
        Page<ProductSummaryResponse> page = productRepository.findSummaries(ProductStatus.ON_SALE, pageable).map(ProductSummaryResponse::from);

        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(Long productId) {
        Product product = productRepository.findWithSkusByIdAndStatus(productId, ProductStatus.ON_SALE).orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));

        return ProductDetailResponse.from(product,
                stockService.findQuantities(
                        product.getSkus().stream().map(Sku::getId).toList()));
    }

    @Transactional(readOnly = true)
    public List<Sku> findOrderableSkus(List<Long> skuIds) {
        List<Sku> skus = skuRepository.findAllWithProductByIdIn(skuIds);

        if (skus.size() != Set.copyOf(skuIds).size()) {
            throw new SkuNotFoundException("판매 단위를 찾을 수 없습니다.");
        }

        for (Sku sku : skus) {
            if (sku.getProduct().getStatus() != ProductStatus.ON_SALE) {
                throw new ProductNotOnSaleException("판매 중지된 상품입니다: " + sku.getProduct().getName());
            }
        }

        return skus;
    }
}

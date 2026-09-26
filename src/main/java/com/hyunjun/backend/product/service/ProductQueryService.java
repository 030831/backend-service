package com.hyunjun.backend.product.service;

import com.hyunjun.backend.common.dto.PageResponse;
import com.hyunjun.backend.product.domain.Product;
import com.hyunjun.backend.product.domain.ProductStatus;
import com.hyunjun.backend.product.domain.Sku;
import com.hyunjun.backend.product.dto.ProductDetailResponse;
import com.hyunjun.backend.product.dto.ProductSummaryResponse;
import com.hyunjun.backend.product.exception.ProductNotFoundException;
import com.hyunjun.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final StockService stockService;

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
}

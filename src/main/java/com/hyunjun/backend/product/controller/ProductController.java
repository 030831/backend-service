package com.hyunjun.backend.product.controller;

import com.hyunjun.backend.common.dto.PageResponse;
import com.hyunjun.backend.product.dto.ProductDetailResponse;
import com.hyunjun.backend.product.dto.ProductSummaryResponse;
import com.hyunjun.backend.product.service.ProductQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;

    @GetMapping
    public PageResponse<ProductSummaryResponse> findOnSale(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return productQueryService.findOnSale(PageRequest.of(page, size));
    }

    @GetMapping("/{productId}")
    public ProductDetailResponse getProduct(@PathVariable Long productId) {
        return productQueryService.getProduct(productId);
    }

}

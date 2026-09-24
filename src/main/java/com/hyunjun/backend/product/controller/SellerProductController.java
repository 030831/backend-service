package com.hyunjun.backend.product.controller;

import com.hyunjun.backend.common.security.LoginPrincipal;
import com.hyunjun.backend.product.dto.*;
import com.hyunjun.backend.product.service.SellerProductService;
import com.hyunjun.backend.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final SellerProductService sellerProductService;
    private final StoreService storeService;

    private Long myStoreId(LoginPrincipal principal) {
        return storeService.getMyStoreId(principal.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SellerProductResponse register(
            @Valid @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal LoginPrincipal principal
    ) {
        return sellerProductService.register(myStoreId(principal), request);
    }

    @GetMapping("/{productId}")
    public SellerProductResponse getProduct(@PathVariable Long productId,
                                            @AuthenticationPrincipal LoginPrincipal principal) {
        return sellerProductService.getProduct(myStoreId(principal), productId);
    }

    @PutMapping("/{productId}")
    public SellerProductResponse update(@PathVariable Long productId,
                                        @Valid @RequestBody ProductUpdateRequest request,
                                        @AuthenticationPrincipal LoginPrincipal principal) {
        return sellerProductService.update(myStoreId(principal), productId, request);
    }

    @PostMapping("/{productId}/stop")
    public SellerProductResponse stop(@PathVariable Long productId,
                                      @AuthenticationPrincipal LoginPrincipal principal) {
        return sellerProductService.stop(myStoreId(principal), productId);
    }

    @PostMapping("/{productId}/resume")
    public SellerProductResponse resume(@PathVariable Long productId,
                                        @AuthenticationPrincipal LoginPrincipal principal) {
        return sellerProductService.resume(myStoreId(principal), productId);
    }

    @PatchMapping("/{productId}/skus/{skuId}/price")
    public SellerProductResponse changePrice(@PathVariable Long productId,
                                             @PathVariable Long skuId,
                                             @Valid @RequestBody SkuPriceRequest request,
                                             @AuthenticationPrincipal LoginPrincipal principal) {
        return sellerProductService.changePrice(myStoreId(principal), productId, skuId, request.price());
    }

    @PatchMapping("/{productId}/skus/{skuId}/stock")
    public StockResponse adjustStock(@PathVariable Long productId,
                                     @PathVariable Long skuId,
                                     @Valid @RequestBody StockAdjustRequest request,
                                     @AuthenticationPrincipal LoginPrincipal principal) {
        return sellerProductService.adjustStock(myStoreId(principal), productId, skuId, request.delta());
    }
}


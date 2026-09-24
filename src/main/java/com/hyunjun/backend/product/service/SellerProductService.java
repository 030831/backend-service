package com.hyunjun.backend.product.service;

import com.hyunjun.backend.product.domain.Product;
import com.hyunjun.backend.product.domain.Sku;
import com.hyunjun.backend.product.dto.*;
import com.hyunjun.backend.product.exception.ProductNotFoundException;
import com.hyunjun.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerProductService {

    private final ProductRepository productRepository;
    private final StockService stockService;

    @Transactional
    public SellerProductResponse register(Long storeId, ProductCreateRequest request) {
        Product product = new Product(storeId, request.name(), request.description());

        for (SkuCreateRequest skuRequest : request.skus()) {
            product.addSku(skuRequest.optionLabel(), skuRequest.price());
        }

        productRepository.save(product);

        List<SkuCreateRequest> skuRequests = request.skus();

        for (int i = 0; i < skuRequests.size(); i++) {
            Sku sku = product.getSkus().get(i);
            stockService.create(sku.getId(), skuRequests.get(i).quantity());
        }

        return toResponse(product);
    }

    private Product getOwnProduct(Long storeId, Long productId) {
        return productRepository.findByIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));
    }

    private SellerProductResponse toResponse(Product product) {
        List<Long> skuIds = product.getSkus().stream().map(Sku::getId).toList();
        return SellerProductResponse.from(product, stockService.findQuantities(skuIds));
    }

    @Transactional(readOnly = true)
    public SellerProductResponse getProduct(Long storeId, Long productId) {
        return toResponse(getOwnProduct(storeId, productId));
    }

    @Transactional
    public SellerProductResponse update(Long storeId, Long productId, ProductUpdateRequest request) {
        Product product = getOwnProduct(storeId, productId);
        product.update(request.name(), request.description());

        return toResponse(product);
    }

    @Transactional
    public SellerProductResponse stop(Long storeId, Long productId) {
        Product product = getOwnProduct(storeId, productId);
        product.stop();
        return toResponse(product);
    }

    @Transactional
    public SellerProductResponse resume(Long storeId, Long productId) {
        Product product = getOwnProduct(storeId, productId);
        product.resume();
        return toResponse(product);
    }

    @Transactional
    public SellerProductResponse changePrice(Long storeId, Long productId, Long skuId, long price) {
        Product product = getOwnProduct(storeId, productId);
        product.getSku(skuId).changePrice(price);

        return toResponse(product);
    }

    @Transactional
    public StockResponse adjustStock(Long storeId, Long productId, Long skuId, int delta) {
        Product product = getOwnProduct(storeId, productId);
        Sku sku = product.getSku(skuId);

        int adjusted = stockService.adjust(skuId, delta);
        return new StockResponse(sku.getId(), adjusted);
    }
}

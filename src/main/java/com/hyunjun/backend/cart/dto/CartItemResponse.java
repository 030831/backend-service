package com.hyunjun.backend.cart.dto;

import com.hyunjun.backend.cart.domain.CartItem;

public record CartItemResponse(Long cartItemId, Long skuId, int quantity) {
    public static CartItemResponse from(CartItem cartItem) {
        return new CartItemResponse(cartItem.getId(), cartItem.getSkuId(), cartItem.getQuantity());
    }
}

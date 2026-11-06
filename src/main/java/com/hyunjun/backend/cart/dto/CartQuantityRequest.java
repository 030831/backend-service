package com.hyunjun.backend.cart.dto;

import com.hyunjun.backend.cart.domain.CartItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartQuantityRequest(
        @NotNull
        @Min(1)
        @Max(CartItem.MAX_QUANTITY)
        Integer quantity
) {
}

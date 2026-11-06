package com.hyunjun.backend.cart.dto;

import com.hyunjun.backend.order.domain.Order;
import com.hyunjun.backend.order.dto.DeliveryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CartOrderRequest(
        @NotBlank(message = "멱등키는 필수입니다.")
        @Size(max = Order.IDEMPOTENCY_KEY_MAX_LENGTH)
        String idempotencyKey,

        @NotEmpty(message = "주문할 장바구니 항목을 1개 이상 골라야 합니다.")
        List<@NotNull Long> cartItemIds,

        @NotNull(message = "배송지는 필수입니다.")
        @Valid
        DeliveryRequest delivery,

        @NotNull(message = "화면에서 본 총액은 필수 입니다.")
        @PositiveOrZero
        Long expectedTotal
) {
}

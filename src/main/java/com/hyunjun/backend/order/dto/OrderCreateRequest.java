package com.hyunjun.backend.order.dto;

import com.hyunjun.backend.order.domain.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record OrderCreateRequest(
        @NotBlank(message = "멱등키는 필수입니다.")
        @Size(max = Order.IDEMPOTENCY_KEY_MAX_LENGTH)
        String idempotencyKey,

        @NotEmpty(message = "주문 항목은 1개 이상이어야 합니다.")
        List<@Valid OrderLineRequest> lines,

        @NotNull(message = "배송지는 필수입니다.")
        @Valid
        DeliveryRequest delivery,

        @NotNull(message = "화면에서 본 총액은 필수 입니다.")
        @PositiveOrZero
        Long expectedTotal
) {

    @AssertTrue(message = "같은 판매 단위가 두 번 들어 있습니다.")
    public boolean isSkuIdsUnique() {
        if (lines == null) {
            return true;
        }

        Set<Long> skuIds = new HashSet<>();

        for (OrderLineRequest line : lines) {
            if (line.skuId() != null && !skuIds.add(line.skuId())) {
                return false;
            }
        }

        return true;
    }
}

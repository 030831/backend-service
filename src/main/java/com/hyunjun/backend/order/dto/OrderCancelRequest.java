package com.hyunjun.backend.order.dto;

import com.hyunjun.backend.order.domain.Order;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCancelRequest(

        @Size(max = Order.MAX_LINES)
        List<@NotNull Long> lineIds
) {
}

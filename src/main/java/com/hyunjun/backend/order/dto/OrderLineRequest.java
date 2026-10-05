package com.hyunjun.backend.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderLineRequest(
        @NotNull
        Long skuId,

        @NotNull @Min(1) @Max(100)
        Integer quantity
){
}

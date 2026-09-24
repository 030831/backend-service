package com.hyunjun.backend.product.dto;

import com.hyunjun.backend.product.domain.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
        @NotBlank(message = "상품 이름은 필수 입니다.")
        @Size(max = Product.NAME_MAX_LENGTH)
        String name,

        @NotBlank(message = "상품 설명은 필수 입니다.")
        @Size(max = Product.DESCRIPTION_MAX_LENGTH)
        String description
) {
}

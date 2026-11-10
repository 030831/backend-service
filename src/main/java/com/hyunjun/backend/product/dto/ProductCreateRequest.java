package com.hyunjun.backend.product.dto;

import com.hyunjun.backend.product.domain.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record ProductCreateRequest(
        @NotBlank(message = "상품 이름은 필수 입니다.")
        @Size(max = Product.NAME_MAX_LENGTH)
        String name,

        @NotBlank(message = "상품 설명은 필수 입니다.")
        @Size(max = Product.DESCRIPTION_MAX_LENGTH)
        String description,

        @NotEmpty(message = "판매 단위는 1개 이상이어야 합니다.")
        @Size(max = Product.MAX_SKUS)
        List<@Valid @NotNull SkuCreateRequest> skus
) {

    @AssertTrue(message = "옵션 라벨이 중복됩니다.")
    public boolean isOptionLabelUnique() {
        if (skus == null) {
            return true;
        }

        Set<String> labels = new HashSet<>();

        for (SkuCreateRequest sku : skus) {
            if (sku != null && sku.optionLabel() != null && !labels.add(sku.optionLabel())) {
                return false;
            }
        }

        return true;
    }
}

package com.hyunjun.backend.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SellerRegistrationRequest {

    @NotBlank(message = "스토어 이름은 필수입니다.")
    @Size(max = 100, message = "스토어 이름은 100자 이하여야 합니다.")
    private String storeName;
}

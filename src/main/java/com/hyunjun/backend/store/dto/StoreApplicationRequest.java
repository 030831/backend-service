package com.hyunjun.backend.store.dto;

import com.hyunjun.backend.store.domain.StoreApplication;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreApplicationRequest(
        @NotBlank(message = "스토어 이름은 필수 입니다.")
        @Size(max = StoreApplication.STORE_NAME_MAX_LENGTH)
        String storeName
) {

}

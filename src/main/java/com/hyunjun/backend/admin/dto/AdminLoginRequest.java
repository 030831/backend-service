package com.hyunjun.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank(message = "이메일은 필수 입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입니다.")
        String password
) {
}

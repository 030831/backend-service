package com.hyunjun.backend.account.dto;

import com.hyunjun.backend.account.domain.Account;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;

public record AccountRegisterRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = Account.EMAIL_MAX_LENGTH, message = "이메일 최대 길이를 초과했습니다.")
        String email,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = Account.NICKNAME_MAX_LENGTH, message = "닉네임 최대 길이를 초과했습니다.")
        String nickname,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password
) {

    @AssertTrue(message = "비밀번호는 72바이트 이하여야 합니다.")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}

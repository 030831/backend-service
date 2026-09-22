package com.hyunjun.backend.account.dto;

import com.hyunjun.backend.account.domain.Account;

public record AccountResponse(Long id, String email, String nickname) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getEmail(), account.getNickname());
    }
}

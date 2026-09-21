package com.hyunjun.backend.account.security;

import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class AccountPrincipal extends User {

    // 업무 코드에 전달할 DB 식별자
    @Getter
    private final Long accountId;

    public AccountPrincipal(
            Long accountId,
            String email,
            String passwordHash
    ) {
        // Security의 기본 사용자 정보에 이메일과 해시를 전달한다.
        // 아직 관리자 등의 권한을 부여하지 않았으므로 빈 목록을 사용한다.
        super(email, passwordHash, List.of());

        this.accountId = accountId;
    }


}

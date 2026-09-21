package com.hyunjun.backend.account.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "accounts", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_accounts_email",
                columnNames = "email"
        ),
        @UniqueConstraint(
                name="uk_accounts_nickname",
                columnNames = "nickname"
        )
})
public class Account extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Getter
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false) @Getter
    private String passwordHash;

    public Account(String email, String nickname, String passwordHash) {
        validate(email, "이메일은 필수입니다.");
        validate(nickname, "닉네임은 필수입니다.");
        validate(passwordHash, "비밀번호 해시 필수입니다.");
        this.email = email;
        this.nickname = nickname;
        this.passwordHash = passwordHash;
    }

    private void validate(String str, String message) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    protected Account() {

    }
}

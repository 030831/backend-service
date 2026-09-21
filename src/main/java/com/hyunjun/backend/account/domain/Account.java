package com.hyunjun.backend.account.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_accounts_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_accounts_nickname", columnNames = "nickname")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseTimeEntity {

    public static final int EMAIL_MAX_LENGTH = 254;
    public static final int NICKNAME_MAX_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = EMAIL_MAX_LENGTH)
    private String email;

    @Column(nullable = false, length = NICKNAME_MAX_LENGTH)
    private String nickname;

    @Column(nullable = false)
    private String passwordHash;

    public Account(String email, String nickname, String passwordHash) {
        this.email = requireText(email, EMAIL_MAX_LENGTH, "이메일");
        this.nickname = requireText(nickname, NICKNAME_MAX_LENGTH, "닉네임");

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("비밀번호 해시가 필요합니다.");
        }

        this.passwordHash = passwordHash;
    }

    private static String requireText(String value, int maxLength, String name) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new IllegalArgumentException(
                    name + "은(는) 비어 있지 않은 " + maxLength + "자 이하여야 합니다."
            );
        }

        return value;
    }
}

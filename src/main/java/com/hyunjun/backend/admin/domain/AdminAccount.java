package com.hyunjun.backend.admin.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_admin_accounts_email", columnNames = "email")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AdminAccount extends BaseTimeEntity {

    public static final int EMAIL_MAX_LENGTH = 254;
    public static final int NAME_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = EMAIL_MAX_LENGTH)
    private String email;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false)
    private String passwordHash;

    public AdminAccount(String email, String name, String passwordHash) {

        this.email = requiredText(email, EMAIL_MAX_LENGTH, "이메일");
        this.name = requiredText(name, NAME_MAX_LENGTH, "이름");

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("비밀번호 해시가 필요합니다.");
        }

        this.passwordHash = passwordHash;
    }

    private static String requiredText(String value, int maxLength, String name) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new IllegalArgumentException(
                    name + "은(는) 비어 있지 않은 " + maxLength + "자 이하여야 합니다."
            );
        }

        return value;
    }
}

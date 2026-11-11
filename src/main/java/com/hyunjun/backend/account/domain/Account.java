package com.hyunjun.backend.account.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
import com.hyunjun.backend.common.domain.DomainText;
import com.hyunjun.backend.common.domain.LoginLock;
import com.hyunjun.backend.common.security.LoginCredential;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
public class Account extends BaseTimeEntity implements LoginCredential {

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

    @Embedded
    private LoginLock loginLock = new LoginLock();

    public Account(String email, String nickname, String passwordHash) {
        this.email = DomainText.required(email, EMAIL_MAX_LENGTH, "이메일");
        this.nickname = DomainText.required(nickname, NICKNAME_MAX_LENGTH, "닉네임");

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("비밀번호 해시가 필요합니다.");
        }

        this.passwordHash = passwordHash;
    }

    @Override
    public boolean isLoginLocked(Instant now) {
        return loginLock.isLocked(now);
    }

    public void recordLoginFailure(Instant now) {
        loginLock.recordFailure(now);
    }

    public boolean hasLoginFailures() {
        return loginLock.hasFailures();
    }

    public void resetLoginFailures() {
        loginLock.reset();
    }
}

package com.hyunjun.backend.store.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
import com.hyunjun.backend.common.domain.DomainText;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseTimeEntity {

    public static final int NAME_MAX_LENGTH = 50;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    Store(Long accountId, String name) {
        if (accountId == null) {
            throw new IllegalArgumentException("계정 아이디가 비어있습니다.");
        }

        this.accountId = accountId;
        this.name = DomainText.required(name, NAME_MAX_LENGTH, "스토어 이름");
    }
}

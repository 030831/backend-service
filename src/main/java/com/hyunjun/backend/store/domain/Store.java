package com.hyunjun.backend.store.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 50)
    private String name;

    Store(Long accountId, String name) {
        if (accountId == null) {
            throw new IllegalArgumentException("계정 아이디가 비어있습니다.");
        }

        if (name == null || name.isBlank() || name.length() > 50) {
            throw new IllegalArgumentException("이름이 비어있거나 50자를 초과하였습니다.");
        }

        this.accountId = accountId;
        this.name = name;
    }
}

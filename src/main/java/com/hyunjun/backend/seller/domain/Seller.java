package com.hyunjun.backend.seller.domain;

import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "sellers")
@Getter
public class Seller extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_sellers_account")
    )
    private Account account;

    @Column(nullable = false, length = 100)
    private String storeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SellerStatus status;

    protected Seller() {

    }

    public Seller(Account account, String storeName) {
        if (account == null || account.getId() == null) {
            throw new IllegalArgumentException("저장된 계정이 필요합니다.");
        }

        if (storeName == null || storeName.isBlank() || storeName.length() > 100) {
            throw new IllegalArgumentException(
                    "스토어 이름은 비어 있지 않은 100자 이하의 문자열이어야 합니다."
            );
        }
        this.account = account;
        this.storeName = storeName;
        this.status = SellerStatus.ACTIVE;
    }
}

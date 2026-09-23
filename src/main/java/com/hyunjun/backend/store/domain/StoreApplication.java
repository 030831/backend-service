package com.hyunjun.backend.store.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreApplication extends BaseTimeEntity {

    public static final int STORE_NAME_MAX_LENGTH = 50;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicantAccountId;

    @Column(nullable = false, length = STORE_NAME_MAX_LENGTH)
    private String storeName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    ApplicationStatus status;

    private Long pendingApplicantId;

    public StoreApplication(Long applicantAccountId, String storeName) {

        if (applicantAccountId == null) {
            throw new IllegalArgumentException("계정 아이디가 비어있습니다.");
        }

        if (storeName == null || storeName.isBlank()) {
            throw new IllegalArgumentException("스토어 이름이 비어있습니다.");
        }

        this.status = ApplicationStatus.SUBMITTED;
        this.pendingApplicantId = applicantAccountId;
        this.applicantAccountId=  applicantAccountId;
        this.storeName = storeName;
    }
}

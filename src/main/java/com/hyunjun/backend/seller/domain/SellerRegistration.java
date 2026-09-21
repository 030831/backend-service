package com.hyunjun.backend.seller.domain;

import com.hyunjun.backend.account.domain.Account;
import com.hyunjun.backend.common.domain.BaseTimeEntity;
import com.hyunjun.backend.common.exception.CommerceConflictException;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Table(name = "seller_registrations")
@Getter
public class SellerRegistration extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_account_id", nullable = false)
    private Account applicant;

    @Column(nullable = false, length = 100)
    private String storeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_account_id")
    private Account reviewedBy;

    private Instant reviewedAt;

    @Column(length = 500)
    private String rejectionReason;

    protected SellerRegistration() {

    }

    public SellerRegistration(Account applicant, String storeName) {
        if (applicant == null || applicant.getId() == null) {
            throw new IllegalArgumentException("지정된 신청 계정이 필요합니다.");
        }

        if (storeName == null || storeName.isBlank() || storeName.length() > 100 ) {
            throw new IllegalArgumentException("스토어 이름은 비어 있지 않은 100자 이하의 문자열이어야 합니다.");
        }

        this.applicant = applicant;
        this.storeName = storeName;
        this.status = RegistrationStatus.SUBMITTED;
    }

    public void requireSubmitted() {
        if (status != RegistrationStatus.SUBMITTED) {
            throw new CommerceConflictException("이미 심사한 신청입니다.");
        }
    }

    public void approve(Account reviewer, Instant now) {
        requireSubmitted();
        requireReviewData(reviewer, now);

        this.status = RegistrationStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = now;
    }

    public void reject(Account reviewer, Instant now, String reason) {
        requireSubmitted();
        requireReviewData(reviewer, now);

        if (reason == null || reason.isBlank() || reason.length() > 500) {
            throw new IllegalArgumentException(
                    "거절 사유는 비어 있지 않은 500자 이하의 문자열이어야 합니다."
            );
        }

        this.status = RegistrationStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewedAt = now;
        this.rejectionReason = reason;
    }


    private void requireReviewData(Account receiver, Instant now) {
        if (receiver == null || receiver.getId() == null || now == null) {
            throw new IllegalArgumentException("심사 계정과 시각이 필요합니다.");
        }
    }
}

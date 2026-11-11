package com.hyunjun.backend.store.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
import com.hyunjun.backend.common.domain.DomainText;
import com.hyunjun.backend.store.exception.AlreadyReviewedApplicationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "store_applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreApplication extends BaseTimeEntity {

    public static final int STORE_NAME_MAX_LENGTH = 50;
    public static final int REJECTION_REASON_MAX_LENGTH = 500;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicantAccountId;

    @Column(nullable = false, length = STORE_NAME_MAX_LENGTH)
    private String storeName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private Long pendingApplicantId;

    private Long reviewerAdminId;

    private Instant reviewedAt;

    @Column(length = REJECTION_REASON_MAX_LENGTH)
    private String rejectionReason;

    public StoreApplication(Long applicantAccountId, String storeName) {

        if (applicantAccountId == null) {
            throw new IllegalArgumentException("계정 아이디가 비어있습니다.");
        }

        this.status = ApplicationStatus.SUBMITTED;
        this.pendingApplicantId = applicantAccountId;
        this.applicantAccountId=  applicantAccountId;
        this.storeName = DomainText.required(storeName, STORE_NAME_MAX_LENGTH, "스토어 이름");
    }

    public Store approve(Long adminId, Instant now) {
        requiredSubmitted();

        this.status = ApplicationStatus.APPROVED;
        markedReviewed(adminId, now);

        return new Store(applicantAccountId, storeName);
    }

    public void reject(Long adminId, Instant now, String reason) {
        requiredSubmitted();

        this.rejectionReason = DomainText.required(reason, REJECTION_REASON_MAX_LENGTH, "반려 사유");
        this.status = ApplicationStatus.REJECTED;
        markedReviewed(adminId, now);
    }

    private void requiredSubmitted() {
        if (status != ApplicationStatus.SUBMITTED) {
            throw new AlreadyReviewedApplicationException("이미 심사한 신청입니다.");
        }
    }

    private void markedReviewed(Long adminId, Instant now) {
        this.reviewerAdminId = adminId;
        this.reviewedAt = now;
        this.pendingApplicantId = null;
    }
}

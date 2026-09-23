package com.hyunjun.backend.store.domain;

import com.hyunjun.backend.common.domain.BaseTimeEntity;
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

    @Column(length = 500)
    private String rejectionReason;

    public StoreApplication(Long applicantAccountId, String storeName) {

        if (applicantAccountId == null) {
            throw new IllegalArgumentException("계정 아이디가 비어있습니다.");
        }

        if (storeName == null || storeName.isBlank() || storeName.length() > STORE_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("스토어 이름이 비어있거나" + STORE_NAME_MAX_LENGTH + "자 초과입니다.");
        }

        this.status = ApplicationStatus.SUBMITTED;
        this.pendingApplicantId = applicantAccountId;
        this.applicantAccountId=  applicantAccountId;
        this.storeName = storeName;
    }

    public Store approve(Long adminId, Instant now) {
        requiredSubmitted();

        this.status = ApplicationStatus.APPROVED;
        markedReviewed(adminId, now);

        return new Store(applicantAccountId, storeName);
    }

    public void reject(Long adminId, Instant now, String reason) {
        requiredSubmitted();

        if (reason == null || reason.isBlank() || reason.length() > 500) {
            throw new IllegalArgumentException("반려 사유는 비어 있지 않은 500자 이하여야 합니다.");
        }

        this.status = ApplicationStatus.REJECTED;
        this.rejectionReason = reason;
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

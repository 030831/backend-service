package com.hyunjun.backend.order.domain;

import com.hyunjun.backend.common.domain.DomainText;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryInfo {

    public static final int RECIPIENT_NAME_MAX_LENGTH = 50;
    public static final int RECIPIENT_PHONE_MAX_LENGTH = 20;
    public static final int ZIP_CODE_MAX_LENGTH = 10;
    public static final int ADDRESS_MAX_LENGTH = 200;
    public static final int MEMO_MAX_LENGTH = 200;

    @Column(nullable = false, length = RECIPIENT_NAME_MAX_LENGTH)
    private String recipientName;

    @Column(nullable = false, length = RECIPIENT_PHONE_MAX_LENGTH)
    private String recipientPhone;

    @Column(nullable = false, length = ZIP_CODE_MAX_LENGTH)
    private String zipCode;

    @Column(nullable = false, length = ADDRESS_MAX_LENGTH)
    private String address1;

    @Column(length = ADDRESS_MAX_LENGTH)
    private String address2;

    @Column(length = MEMO_MAX_LENGTH)
    private String deliveryMemo;

    public DeliveryInfo(String recipientName, String recipientPhone, String zipCode, String address1, String address2, String deliveryMemo) {
        this.recipientName = DomainText.required(recipientName, RECIPIENT_NAME_MAX_LENGTH, "받는 사람");
        this.recipientPhone = DomainText.required(recipientPhone, RECIPIENT_PHONE_MAX_LENGTH, "연락처");
        this.zipCode = DomainText.required(zipCode, ZIP_CODE_MAX_LENGTH, "우편번호");
        this.address1 = DomainText.required(address1, ADDRESS_MAX_LENGTH, "주소");
        this.address2 = DomainText.optional(address2, ADDRESS_MAX_LENGTH, "상세 주소");
        this.deliveryMemo = DomainText.optional(deliveryMemo, MEMO_MAX_LENGTH, "배송 메모");
    }
}

package com.hyunjun.backend.order.dto;

import com.hyunjun.backend.order.domain.DeliveryInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliveryRequest(

        @NotBlank @Size(max = DeliveryInfo.RECIPIENT_NAME_MAX_LENGTH)
        String recipientName,

        @NotBlank @Size(max = DeliveryInfo.RECIPIENT_PHONE_MAX_LENGTH)
        String recipientPhone,

        @NotBlank @Size(max = DeliveryInfo.ZIP_CODE_MAX_LENGTH)
        String zipCode,

        @NotBlank @Size(max = DeliveryInfo.ADDRESS_MAX_LENGTH)
        String address1,

        @Size(max = DeliveryInfo.ADDRESS_MAX_LENGTH)
        String address2,

        @Size(max = DeliveryInfo.MEMO_MAX_LENGTH)
        String deliveryMemo
) {

    public DeliveryInfo toDeliveryInfo() {
        return new DeliveryInfo(recipientName,
                recipientPhone,
                zipCode,
                address1,
                address2,
                deliveryMemo);
    }
}

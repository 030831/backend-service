package com.hyunjun.backend.order.dto;

import com.hyunjun.backend.order.domain.DeliveryInfo;

public record DeliveryResponse(
        String recipientName,
        String recipientPhone,
        String zipCode,
        String address1,
        String address2,
        String deliveryMemo
) {
    public static DeliveryResponse from(DeliveryInfo delivery) {
        return new DeliveryResponse(
                delivery.getRecipientName(),
                delivery.getRecipientPhone(),
                delivery.getZipCode(),
                delivery.getAddress1(),
                delivery.getAddress2(),
                delivery.getDeliveryMemo()
        );
    }
}

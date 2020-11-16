package com.foodtech.back.util.exceptions;

import lombok.Getter;

@Getter
public class CloudPaymentPayTransactionDeclinedException extends RuntimeException {

    private Long orderId;
    private String declineReason;

    public CloudPaymentPayTransactionDeclinedException(Long orderId, String declineReason) {
        this.orderId = orderId;
        this.declineReason = declineReason;
    }
}

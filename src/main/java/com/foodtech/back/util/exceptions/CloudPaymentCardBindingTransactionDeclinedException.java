package com.foodtech.back.util.exceptions;

import lombok.Getter;

@Getter
public class CloudPaymentCardBindingTransactionDeclinedException extends RuntimeException {

    private String declineReason;

    public CloudPaymentCardBindingTransactionDeclinedException(String declineReason) {
        this.declineReason = declineReason;
    }
}

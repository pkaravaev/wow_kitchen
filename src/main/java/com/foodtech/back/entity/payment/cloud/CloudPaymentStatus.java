package com.foodtech.back.entity.payment.cloud;

import lombok.Getter;

@Getter
public enum CloudPaymentStatus {

    AwaitingAuthentication,
    Authorized,
    Completed,
    Cancelled,
    Declined
}

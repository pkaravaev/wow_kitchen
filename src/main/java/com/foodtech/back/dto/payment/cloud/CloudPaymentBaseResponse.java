package com.foodtech.back.dto.payment.cloud;

import lombok.Data;

import static org.springframework.util.StringUtils.hasText;

@Data
public class CloudPaymentBaseResponse  {

    private static final Integer CLOUD_PAYMENT_SUCCESS_CODE = 0;

    private CloudPaymentResponse model;

    private boolean success;

    private String message;

//  https://cloudpayments.kz/Docs/Api#payWithCrypto
    public boolean transactionDeclined() {
        return !success
                && !CLOUD_PAYMENT_SUCCESS_CODE.equals(model.getReasonCode())
                && !hasText(model.getPaReq());
    }

    public boolean transactionApproved() {
        return success && model.getReasonCode().equals(0);
    }

    public boolean isNeed3DSResponse() {
        return !success && hasText(model.getPaReq()) && hasText(model.getAcsUrl());
    }
}

package com.foodtech.back.service.payment.cloud;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.payment.cloud.CloudPaymentRequest;
import org.springframework.stereotype.Component;

import static com.foodtech.back.dto.payment.cloud.CloudPaymentRequestType.*;

@Component
public class CloudPaymentRequestBuilder {

    private final ResourcesProperties properties;

    public CloudPaymentRequestBuilder(ResourcesProperties properties) {
        this.properties = properties;
    }

    public CloudPaymentRequest bindCardRequest(Long userId, String crypto) {
        return CloudPaymentRequest.builder()
                .type(CARD_BIND)
                .amount(properties.getPaymentCardBindingAmount())
                .cardCryptogramPacket(crypto)
                .accountId(String.valueOf(userId))
                .currency(properties.getPaymentCurrency())
                .description(properties.getCardBindingDescription())
                .build();
    }

    public CloudPaymentRequest authRequest(Long userId, String cardToken, Integer amount, Long orderId) {
        return CloudPaymentRequest.builder()
                .type(AUTH)
                .amount(amount)
                .token(cardToken)
                .invoiceId(orderId.toString())
                .accountId(userId.toString())
                .currency(properties.getPaymentCurrency())
                .description(properties.getPaymentDescription())
                .build();
    }

    CloudPaymentRequest authGooglePayRequest(Long userId, String googlePaymentData, Integer amount, Long orderId) {
        return CloudPaymentRequest.builder()
                .type(GOOGLE_PAY_AUTH)
                .amount(amount)
                .cardCryptogramPacket(googlePaymentData)
                .invoiceId(orderId.toString())
                .accountId(userId.toString())
                .currency(properties.getPaymentCurrency())
                .description(properties.getPaymentDescription())
                .build();
    }

    public CloudPaymentRequest cancelAuthRequest(Long transactionId) {
        return CloudPaymentRequest.builder()
                .type(CANCEL_AUTH)
                .transactionId(transactionId)
                .build();
    }

    public CloudPaymentRequest confirmAuthRequest(Long transactionId, Integer totalCost) {
        return CloudPaymentRequest.builder()
                .type(CONFIRM_AUTH)
                .transactionId(transactionId)
                .amount(totalCost)
                .build();
    }

    public CloudPaymentRequest paResRequest(Long transactionId, String paRes) {
        return CloudPaymentRequest.builder()
                .type(PA_RES)
                .transactionId(transactionId)
                .paRes(paRes)
                .build();
    }
}

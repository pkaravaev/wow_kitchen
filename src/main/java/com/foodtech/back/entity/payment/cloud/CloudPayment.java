package com.foodtech.back.entity.payment.cloud;

import com.foodtech.back.dto.payment.cloud.CloudPaymentResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class CloudPayment {

    private Long transactionId;

    @Enumerated(EnumType.STRING)
    private CloudPaymentStatus cloudPaymentStatus;

    /* очередь для отслеживания статуса оплаты заказа при оплате с 3DS
       (пока такой вариант возможен тотлько при оплате через GooglePay) */
    private String paymentQueueName;

    @Column(name = "payment_decline_reason")
    private String declineReason;

    @Column(name = "payment_decline_reason_code")
    private Integer declineReasonCode;

    private boolean paymentCompleteRequired;

    private CloudPayment(CloudPaymentResponse model) {
        this.transactionId = model.getTransactionId();
        this.cloudPaymentStatus = model.getStatus();
        this.declineReason = model.getReason();
        this.declineReasonCode = model.getReasonCode();
    }

    public static CloudPayment fromResponseModel(CloudPaymentResponse model) {
        return new CloudPayment(model);
    }
}

package com.foodtech.back.service.event.card;

import com.foodtech.back.entity.payment.cloud.BankCardBindRequest;

public class CardBindDeclinedEvent extends AbstractCardBindEvent {
    public CardBindDeclinedEvent(BankCardBindRequest bindRequest) {
        super(bindRequest);
    }
}

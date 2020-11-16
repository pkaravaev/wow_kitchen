package com.foodtech.back.service.event.card;

import com.foodtech.back.entity.payment.cloud.BankCardBindRequest;

public class CardBindSuccessEvent extends AbstractCardBindEvent {

    public CardBindSuccessEvent(BankCardBindRequest bindRequest) {
        super(bindRequest);
    }
}

package com.foodtech.back.service.event.card;

import com.foodtech.back.entity.payment.cloud.BankCardBindRequest;
import lombok.Getter;

@Getter
public class CardBindFailedEvent extends AbstractCardBindEvent {
    public CardBindFailedEvent(BankCardBindRequest bindRequest) {
        super(bindRequest);
    }
}

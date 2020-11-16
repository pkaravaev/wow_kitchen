package com.foodtech.back.service.event.card;

import com.foodtech.back.entity.payment.cloud.BankCardBindRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public abstract class AbstractCardBindEvent {

    private BankCardBindRequest bindRequest;

    public AbstractCardBindEvent(BankCardBindRequest bindRequest) {
        this.bindRequest = bindRequest;
    }
}

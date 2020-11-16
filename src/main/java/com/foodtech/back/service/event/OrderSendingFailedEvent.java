package com.foodtech.back.service.event;

import com.foodtech.back.dto.iiko.IikoOrderSendingResult;
import com.foodtech.back.entity.model.Order;
import lombok.Getter;

@Getter
public class OrderSendingFailedEvent extends OrderStatusChangedEvent {

    private IikoOrderSendingResult sendingResult;

    public OrderSendingFailedEvent(Order order, IikoOrderSendingResult sendingResult) {
        super(order);
        this.sendingResult = sendingResult;
    }
}

package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;

public class OrderNotConfirmedEvent extends OrderStatusChangedEvent {

    public OrderNotConfirmedEvent(Order order) {
        super(order);
    }
}

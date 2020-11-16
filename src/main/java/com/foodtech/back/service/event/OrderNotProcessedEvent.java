package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;

public class OrderNotProcessedEvent extends OrderStatusChangedEvent {

    public OrderNotProcessedEvent(Order order) {
        super(order);
    }
}

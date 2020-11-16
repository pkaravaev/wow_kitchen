package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;

public class OrderReadyEvent extends OrderStatusChangedEvent {
    public OrderReadyEvent(Order order) {
        super(order);
    }
}

package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;

public class OrderCancelledEvent extends OrderStatusChangedEvent {
    public OrderCancelledEvent(Order order) {
        super(order);
    }
}

package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;

public class OrderClosedEvent extends OrderStatusChangedEvent {

    public OrderClosedEvent(Order order) {
        super(order);
    }
}

package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;

public class OrderDeliveredEvent extends OrderStatusChangedEvent {

    public OrderDeliveredEvent(Order order) {
        super(order);
    }
}

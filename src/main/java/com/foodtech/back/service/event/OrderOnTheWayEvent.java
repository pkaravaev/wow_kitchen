package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;

public class OrderOnTheWayEvent extends OrderStatusChangedEvent {

    public OrderOnTheWayEvent(Order order) {
        super(order);
    }
}

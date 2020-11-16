package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;

public class OrderInProgressEvent extends OrderStatusChangedEvent {

    public OrderInProgressEvent(Order order) {
        super(order);
    }
}

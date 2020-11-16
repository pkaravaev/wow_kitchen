package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;

public class OrderAwaitingDeliveryEvent extends OrderStatusChangedEvent {
    public OrderAwaitingDeliveryEvent(Order order) {
        super(order);
    }
}

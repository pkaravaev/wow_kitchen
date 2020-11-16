package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;

public class BonusCollisionEvent extends OrderStatusChangedEvent {

    public BonusCollisionEvent(Order order) {
        super(order);
    }
}

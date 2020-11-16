package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;
import lombok.Getter;

@Getter
public abstract class OrderStatusChangedEvent {

    private Order order;

    public OrderStatusChangedEvent(Order order) {
        this.order = order;
    }
}

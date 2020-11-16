package com.foodtech.back.service.event;


import com.foodtech.back.entity.model.Order;

public class OrderPaidEvent extends OrderStatusChangedEvent {

    public OrderPaidEvent(Order order) {
        super(order);
    }
}

package com.foodtech.back.service.event;

import com.foodtech.back.entity.model.Order;
import lombok.Getter;

@Getter
public class OrderNotPaidEvent extends OrderStatusChangedEvent {

    public OrderNotPaidEvent(Order order) {
        super(order);
    }
}

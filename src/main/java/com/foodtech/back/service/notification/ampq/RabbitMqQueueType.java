package com.foodtech.back.service.notification.ampq;

import lombok.Getter;

@Getter
public enum RabbitMqQueueType {

    PAYMENT_QUEUE,
    CARD_BINDING_QUEUE,
    ORDER_STATUS_QUEUE,
}

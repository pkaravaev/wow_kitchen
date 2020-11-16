package com.foodtech.back.service.notification.push;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.service.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class FirebasePushEventListeners {
    
    private final FirebasePushSender sender;
    
    private final ResourcesProperties properties;

    public FirebasePushEventListeners(FirebasePushSender sender, ResourcesProperties properties) {
        this.sender = sender;
        this.properties = properties;
    }

    @EventListener
    public void sendBonusCollisionPush(BonusCollisionEvent event) {
        sender.send(event.getOrder().getUser().getId(), properties.getOrderSentBonusCollisionPush());
    }

    @EventListener
    public void sendOrderSentPush(OrderSentEvent event) {
        sender.send(event.getOrder().getUser().getId(), properties.getOrderSentSuccessPush());
    }

    @EventListener
    public void sendOrderSendingFailedPush(OrderSendingFailedEvent event) {
        sender.send(event.getOrder().getUser().getId(), properties.getOrderSendingFailedPush());
    }

    @EventListener
    public void sendOrderInProgressPush(OrderInProgressEvent event) {
        sender.send(event.getOrder().getUser().getId(), properties.getOrderInProgressPush());
    }

    @EventListener
    public void sendOrderOnTheWayPush(OrderOnTheWayEvent event) {
        sender.send(event.getOrder().getUser().getId(), properties.getOrderOnTheWayPush());
    }

    @EventListener
    public void sendOrderClosedPush(OrderClosedEvent event) {
        sender.send(event.getOrder().getUser().getId(), properties.getOrderDeliveredPush());
    }

    @EventListener
    public void sendOrderCancelledPush(OrderCancelledEvent event) {
        sender.send(event.getOrder().getUser().getId(), properties.getOrderCancelledPush());
    }

}

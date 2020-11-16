package com.foodtech.back.service.notification.ampq;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.entity.payment.PaymentType;
import com.foodtech.back.service.event.*;
import com.foodtech.back.service.event.card.CardBindDeclinedEvent;
import com.foodtech.back.service.event.card.CardBindFailedEvent;
import com.foodtech.back.service.event.card.CardBindSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static com.foodtech.back.service.payment.cloud.CloudPaymentService.BAD_PaRes_REQUEST;

@Service
@Slf4j
public class RabbitMqEventListeners {
    
    private final RabbitMqService mqService;

    private final ResourcesProperties properties;

    public RabbitMqEventListeners(RabbitMqService mqService, ResourcesProperties properties) {
        this.mqService = mqService;
        this.properties = properties;
    }

    @EventListener
    public void sendCardBindSuccessMsg(CardBindSuccessEvent event) {
        mqService.sendMessage(event.getBindRequest().getQueueName(), properties.getCardBindingSuccessRabbitMqMsg());
    }

    @EventListener
    public void sendCardBindFailedMsg(CardBindFailedEvent event) {
        mqService.sendMessage(event.getBindRequest().getQueueName(), properties.getCardBindingFailedRabbitMqMsg());
    }

    @EventListener
    public void sendCardBindDeclinedMsg(CardBindDeclinedEvent event) {
        mqService.sendMessage(event.getBindRequest().getQueueName(), event.getBindRequest().getResult());
    }

    @EventListener
    public void sendOrderPaymentFailedMsg(OrderNotPaidEvent event) {
        String declineReason = event.getOrder().getCloudPayment().getDeclineReason();
        String msg = BAD_PaRes_REQUEST.equals(declineReason) ? properties.getPaymentFailedRabbitMqMsg() : declineReason;
        mqService.sendMessage(event.getOrder().getStatusQueueName(), msg);
    }

    // Только на случай оплаты через 3DS (в настоящий момент возможно только при оплате через GooglePay),
    // чтобы оповестить фронт о том, что авторизация 3DS успешно пройдена и заказ, соответственно, оплачен
    @EventListener
    public void sendOrderPaymentSuccessMsg(OrderPaidEvent event) {
        if (PaymentType.GOOGLE_PAY == event.getOrder().getPaymentType()) {
            mqService.sendMessage(event.getOrder().getStatusQueueName(), properties.getPaymentSuccessRabbitMqMsg());
        }
    }

    @EventListener
    public void sendOrderSendingFailedMsg(OrderSendingFailedEvent event) {
        mqService.sendMessage(event.getOrder().getStatusQueueName(), properties.getOrderSendingFailedRabbitMqMsg());
    }

    @EventListener
    public void sendOrderDeliveredMsg(OrderDeliveredEvent event) {
        mqService.sendMessage(event.getOrder().getStatusQueueName(), properties.getOrderDeliveredRabbitMqMsg());
    }

    @EventListener
    public void sendOrderClosedMsg(OrderClosedEvent event) {
        mqService.sendMessage(event.getOrder().getStatusQueueName(), properties.getOrderDeliveredRabbitMqMsg());
    }

    @EventListener
    public void sendOrderCancelled(OrderCancelledEvent event) {
        mqService.sendMessage(event.getOrder().getStatusQueueName(), properties.getOrderCancelledRabbitMqMsg());
    }

    @EventListener
    public void sendOrderNotProcessed(OrderNotProcessedEvent event) {
        mqService.sendMessage(event.getOrder().getStatusQueueName(), properties.getOrderCancelledRabbitMqMsg());
    }
}

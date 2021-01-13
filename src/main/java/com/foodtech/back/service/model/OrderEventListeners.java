package com.foodtech.back.service.model;

import com.foodtech.back.service.event.*;
import com.foodtech.back.service.iiko.IikoRequestService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventListeners {

    private final OrderService orderService;

    private final IikoRequestService iikoRequestService;

    public OrderEventListeners(OrderService orderService, IikoRequestService iikoRequestService) {
        this.orderService = orderService;
        this.iikoRequestService = iikoRequestService;
    }

    @EventListener
    public void orderPaid(OrderPaidEvent event) {
        orderService.processPaid(event.getOrder().getId());
        iikoRequestService.sendOrder(event.getOrder().getId());
    }

    @EventListener
    public void orderSendingFailed(OrderSendingFailedEvent event) {
        orderService.processIikoSendingFail(event.getOrder().getId(), event.getSendingResult());
    }

    @EventListener
    public void orderSent(OrderSentEvent event) {
        orderService.processIikoSendingSuccess(event.getOrder().getId(), event.getSendingResult());
    }

    @EventListener
    public void orderInProgress(OrderInProgressEvent event) {
        orderService.processInProgress(event.getOrder().getId());
    }

    @EventListener
    public void orderReady(OrderReadyEvent event) {
        orderService.processReady(event.getOrder().getId());
    }

    @EventListener
    public void orderAwaitingDelivery(OrderAwaitingDeliveryEvent event) {
        orderService.processAwaitingDelivery(event.getOrder().getId());
    }

    @EventListener
    public void orderOnTheWay(OrderOnTheWayEvent event) {
        orderService.processOnTheWay(event.getOrder().getId());
    }

    @EventListener
    public void orderDelivered(OrderDeliveredEvent event) {
        orderService.processDelivered(event.getOrder().getId());
    }

    @EventListener
    public void orderClosed(OrderClosedEvent event) {
        orderService.processClosed(event.getOrder().getId());
    }

    @EventListener
    public void orderCancelled(OrderCancelledEvent event) {
       // orderService.processCancelled(event.getOrder().getId());
    }

    @EventListener
    public void orderNotConfirmed(OrderNotConfirmedEvent event) {
        orderService.processNotConfirmed(event.getOrder().getId());
    }

    @EventListener
    public void orderNotProcessed(OrderNotProcessedEvent event) {
        orderService.processNotProcessed(event.getOrder().getId());
    }
}

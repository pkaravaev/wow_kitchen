package com.foodtech.back.unit.service.model;

import com.foodtech.back.dto.iiko.IikoOrderInfo;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.OrderStatus;
import com.foodtech.back.service.event.*;
import com.foodtech.back.service.http.IikoHttpSender;
import com.foodtech.back.service.iiko.IikoOrderStatusCheckExecutor;
import com.foodtech.back.service.model.OrderServiceImpl;
import com.foodtech.back.unit.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static com.foodtech.back.entity.model.OrderStatus.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class IikoOrderStatusCheckerTaskUnitTest extends AbstractUnitTest {

    @MockBean
    IikoHttpSender httpSender;

    @MockBean
    ApplicationEventPublisher publisher;

    @MockBean
    ThreadPoolTaskExecutor executor;

    @MockBean
    OrderServiceImpl orderService;

    @Test
    void statusPaid() {
        formTask(order(PAID)).run();

        verify(publisher, times(1)).publishEvent(any(OrderPaidEvent.class));
        verifyNoMoreInteractions(publisher);
    }

    @Test
    void statusPaidSendingTimeExpired() {
        Order order = order(PAID);
        order.setPaidTime(LocalDateTime.of(2019, 1, 1, 1, 1));

        formTask(order).run();

        verify(publisher, times(1)).publishEvent(any(OrderSendingFailedEvent.class));
        verifyNoMoreInteractions(publisher);
    }

    @Test
    void statusChanged() {
        testStatusChangedFromTo(SENT, IN_PROGRESS);
        verify(publisher, times(1)).publishEvent(any(OrderInProgressEvent.class));

        testStatusChangedFromTo(IN_PROGRESS, READY);
        verify(publisher, times(1)).publishEvent(any(OrderReadyEvent.class));

        testStatusChangedFromTo(READY, AWAITING_DELIVERY);
        verify(publisher, times(1)).publishEvent(any(OrderAwaitingDeliveryEvent.class));

        testStatusChangedFromTo(AWAITING_DELIVERY, ON_THE_WAY);
        verify(publisher, times(1)).publishEvent(any(OrderOnTheWayEvent.class));

        testStatusChangedFromTo(ON_THE_WAY, DELIVERED);
        verify(publisher, times(1)).publishEvent(any(OrderDeliveredEvent.class));

        testStatusChangedFromTo(DELIVERED, CLOSED);
        verify(publisher, times(1)).publishEvent(any(OrderClosedEvent.class));

        testStatusChangedFromTo(SENT, NOT_CONFIRMED);
        verify(publisher, times(1)).publishEvent(any(OrderNotConfirmedEvent.class));

        testStatusChangedFromTo(SENT, CANCELLED);
        verify(publisher, times(1)).publishEvent(any(OrderCancelledEvent.class));

        verifyNoMoreInteractions(publisher);
    }

    @Test
    void statusNotChanged() {
        testStatusChangedFromTo(IN_PROGRESS, IN_PROGRESS);
        verifyZeroInteractions(publisher);
    }

    @Test
    void checkTimeExpired() {
        IikoOrderInfo orderInfo = new IikoOrderInfo();
        orderInfo.setStatus(IN_PROGRESS.getIikoStatusRus());
        when(httpSender.sendGetOrderInfo(IIKO_ORDER_ID)).thenReturn(orderInfo);
        Order order = order(IN_PROGRESS);
        order.setCreated(LocalDateTime.of(2019, 1, 1, 1, 1));

        formTask(order).run();

        verify(publisher, times(1)).publishEvent(any(OrderNotProcessedEvent.class));
        verifyNoMoreInteractions(publisher);
    }

    @Test
    void checkRequestFailed() {
        when(httpSender.sendGetOrderInfo(IIKO_ORDER_ID)).thenReturn(null);

        formTask(order(IN_PROGRESS)).run();
        verifyZeroInteractions(publisher);
    }

    @Test
    void statusParseFailed() {
        IikoOrderInfo orderInfo = new IikoOrderInfo();
        orderInfo.setStatus("Unknown iiko status");
        when(httpSender.sendGetOrderInfo(IIKO_ORDER_ID)).thenReturn(orderInfo);

        assertThrows(NoSuchElementException.class, () -> formTask(order(IN_PROGRESS)).run());
        verifyZeroInteractions(publisher);
    }

    private void testStatusChangedFromTo(OrderStatus prevStatus, OrderStatus newStatus) {
        IikoOrderInfo orderInfo = new IikoOrderInfo();
        orderInfo.setStatus(newStatus.getIikoStatusRus());
        when(httpSender.sendGetOrderInfo(IIKO_ORDER_ID)).thenReturn(orderInfo);

        formTask(order(prevStatus)).run();
    }

    private IikoOrderStatusCheckExecutor.IikoStatusCheckerTask formTask(Order order) {
        IikoOrderStatusCheckExecutor checkExecutor = new IikoOrderStatusCheckExecutor(executor, orderService, publisher, httpSender);
        return checkExecutor.new IikoStatusCheckerTask(order);
    }

    private final static String IIKO_ORDER_ID = "IIKO_ORDER_ID";

    private Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(1L);
        order.setIikoOrderId(IIKO_ORDER_ID);
        order.setStatus(status);
        order.setPaidTime(LocalDateTime.now());
        order.setCreated(LocalDateTime.now());
        return order;
    }
}

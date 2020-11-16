package com.foodtech.back.unit.service.model;

import com.foodtech.back.dto.iiko.IikoCheckOrderResponse;
import com.foodtech.back.dto.iiko.IikoOrderInfo;
import com.foodtech.back.dto.iiko.IikoOrderRequest;
import com.foodtech.back.entity.model.*;
import com.foodtech.back.entity.model.iiko.Product;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.service.event.OrderSendingFailedEvent;
import com.foodtech.back.service.event.OrderSentEvent;
import com.foodtech.back.service.http.IikoHttpSender;
import com.foodtech.back.service.iiko.IikoRequestService;
import com.foodtech.back.unit.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static com.foodtech.back.dto.iiko.IikoCheckOrderResponse.IIKO_OK_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IikoRequestServiceUnitTest extends AbstractUnitTest {

    @MockBean
    IikoHttpSender requestSender;

    @MockBean
    OrderRepository orderRepository;

    @MockBean
    ApplicationEventPublisher eventPublisher;

    @Test
    void sendOrder() {
        Order order = order();
        when(orderRepository.findByIdFetchUserAndProductsAndAddress(order.getId())).thenReturn(Optional.of(order));
        IikoCheckOrderResponse checkResponse = new IikoCheckOrderResponse();
        checkResponse.setResultState(IIKO_OK_CODE);
        when(requestSender.sendCheckOrderRequest(any(IikoOrderRequest.class))).thenReturn(checkResponse);
        when(requestSender.sendOrderRequest(any(IikoOrderRequest.class))).thenReturn(new IikoOrderInfo());

        requestService().sendOrder(order.getId());

        verify(eventPublisher, times(1)).publishEvent(any(OrderSentEvent.class));
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    void sendOrder_CheckFailed() {
        Order order = order();
        when(orderRepository.findByIdFetchUserAndProductsAndAddress(order.getId())).thenReturn(Optional.of(order));
        IikoCheckOrderResponse checkResponse = new IikoCheckOrderResponse();
        checkResponse.setResultState(IIKO_OK_CODE+1);
        when(requestSender.sendCheckOrderRequest(any(IikoOrderRequest.class))).thenReturn(checkResponse);

        requestService().sendOrder(order.getId());

        verify(eventPublisher, times(1)).publishEvent(any(OrderSendingFailedEvent.class));
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    void sendOrderFailed() {
        Order order = order();
        when(orderRepository.findByIdFetchUserAndProductsAndAddress(order.getId())).thenReturn(Optional.of(order));
        IikoCheckOrderResponse checkResponse = new IikoCheckOrderResponse();
        checkResponse.setResultState(IIKO_OK_CODE);
        when(requestSender.sendCheckOrderRequest(any(IikoOrderRequest.class))).thenReturn(checkResponse);
        when(requestSender.sendOrderRequest(any(IikoOrderRequest.class))).thenReturn(null);

        requestService().sendOrder(order.getId());

        verify(eventPublisher, times(1)).publishEvent(any(OrderSendingFailedEvent.class));
        verifyNoMoreInteractions(eventPublisher);
    }

    // Из-за того, что ApplicationEventPublisher не может быть замокан при обычном @Autowired сервиса,
    // приходится вручную создавать экземпляр сервиса. В этом случае ApplicationEventPublisher мокируется корректно
    // https://github.com/spring-projects/spring-boot/issues/6060
    private IikoRequestService requestService() {
        return new IikoRequestService(requestSender, orderRepository, eventPublisher);
    }

    private static Order order() {
        Order order = new Order();
        order.setId(1L);
        order.setTotalCost(100);
        order.setUser(new User());
        Address address = new Address();
        address.setHome("1A");
        order.setAddress(address);
        KitchenDeliveryTerminal terminal = new KitchenDeliveryTerminal();
        address.setDeliveryTerminal(terminal);
        Kitchen kitchen = new Kitchen();
        kitchen.setPaymentTypeExternalRevision(2222L);
        terminal.setKitchen(kitchen);

        OrderItem item = new OrderItem();
        item.setAmount(1);
        order.setItems(List.of(item));
        Product product = new Product();
        product.setPrice(100);
        item.setProduct(product);

        return order;
    }

}
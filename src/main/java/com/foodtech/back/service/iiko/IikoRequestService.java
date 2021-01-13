package com.foodtech.back.service.iiko;

import com.foodtech.back.dto.iiko.*;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.service.event.OrderSendingFailedEvent;
import com.foodtech.back.service.event.OrderSentEvent;
import com.foodtech.back.service.http.IikoHttpSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.foodtech.back.dto.iiko.IikoOrderRequest.form;
import static com.foodtech.back.dto.iiko.IikoOrderSendingResult.failed;
import static com.foodtech.back.dto.iiko.IikoOrderSendingResult.success;
import static java.util.Objects.isNull;

@Service
@Slf4j
public class IikoRequestService {

    private final IikoHttpSender requestSender;

    private final OrderRepository orderRepository;

    private final ApplicationEventPublisher publisher;

    public IikoRequestService(IikoHttpSender requestSender, OrderRepository orderRepository,
                              ApplicationEventPublisher publisher) {
        this.requestSender = requestSender;
        this.orderRepository = orderRepository;
        this.publisher = publisher;
    }

    @Transactional
    public void sendOrder(Long orderId) {

        Order order = orderRepository.findByIdFetchUserAndProductsAndAddress(orderId).orElseThrow();
        IikoOrderRequest request = form(order);

        // Запрос на проверку возможности создания такого заказа в iiko
        IikoCheckOrderResponse checkOrderResponse = requestSender.sendCheckOrderRequest(request);
        if (!checkOrderResponse.orderCanBeSend()) {
            log.error("Iiko order request failed. Order: '{}'. User: '{}'", order.getId(), order.getUser());
            publisher.publishEvent(new OrderSendingFailedEvent(order, failed(checkOrderResponse.getProblem())));
            return;
        }

        IikoOrderInfo createdOrderInfo = requestSender.sendOrderRequest(request);
        if (isNull(createdOrderInfo)) {
            log.error("Iiko order sending failed. Order '{}'. User '{}'", order.getId(), order.getUser());
            publisher.publishEvent(new OrderSendingFailedEvent(order, failed()));
            return;
        }
        order.setIikoOrderId(createdOrderInfo.getOrderId());
        orderRepository.save(order);
        publisher.publishEvent(new OrderSentEvent(order, success(createdOrderInfo)));
    }

    public IikoNomenclatureDto getNomenclature() {
        return requestSender.sendNomenclatureRequest();
    }

    public Set<String> getStopList() {
        IikoStopListDto stopListDto = requestSender.sendStopListRequest();
        if (isNull(stopListDto)) {
            return null;
        }

        if (isNull(stopListDto.getStopList()) || stopListDto.getStopList().isEmpty()) {
            return Collections.emptySet();
        }
        return stopListDto.getStopList().get(0).getItemsIdList();
    }

    public List<IikoDeliveryZoneDto> getDeliveryZones() {
        return requestSender.sendDeliveryZoneRequest();
    }

    public List<IikoCityStreetsDirectoryDto> getCityStreetsDirectory() {
        return requestSender.sendCityStreetsDirectoryRequest();
    }

}

package com.foodtech.back.service.model;

import com.foodtech.back.dto.iiko.IikoOrderSendingResult;
import com.foodtech.back.dto.model.OrderRegistrationDto;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.OrderItem;
import com.foodtech.back.entity.model.OrderStatus;
import com.foodtech.back.entity.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    Page<Order> getAllForAdmin(PageRequest pageRequest);

    List<OrderItem> getItemsByOrderIdForAdmin(Long orderId);

    List<Order> getAllByUserWithItemsAndAddress(User user);

    Optional<Order> getByIdAndUserWithItemsAndAddress(Long orderId, Long userId);

    List<Long> getPaymentConfirmRequiredIds();

    List<Long> getPaymentCancelRequiredIds();

    Order register(Long userId, OrderRegistrationDto orderDto);

    OrderStatus getStatusByIdAndUserId(Long orderId, Long userId);

    List<Order> getForStatusChecking();

    Optional<Order> getUserOrderInProcessing(Long id);

    void processPaid(Long orderId);

    void processIikoSendingSuccess(Long orderId, IikoOrderSendingResult sendingResult);

    void processIikoSendingFail(Long orderId, IikoOrderSendingResult sendingResult);

    void processInProgress(Long orderId);

    void processReady(Long orderId);

    void processAwaitingDelivery(Long orderId);

    void processOnTheWay(Long orderId);

    void processDelivered(Long orderId);

    void processClosed(Long orderId);

    void processCancelled(Long orderId);

    void processNotConfirmed(Long orderId);

    void processNotProcessed(Long orderId);
}

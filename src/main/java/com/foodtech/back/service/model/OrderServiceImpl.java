package com.foodtech.back.service.model;

import com.foodtech.back.bot.WowKitchenBot;
import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.iiko.IikoOrderSendingResult;
import com.foodtech.back.dto.model.OrderRegistrationDto;
import com.foodtech.back.entity.model.*;
import com.foodtech.back.entity.model.iiko.Product;
import com.foodtech.back.repository.model.OrderItemRepository;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.service.bonus.BonusService;
import com.foodtech.back.service.notification.ampq.RabbitMqQueueType;
import com.foodtech.back.service.notification.ampq.RabbitMqService;
import com.foodtech.back.util.exceptions.CartInvalidException;
import com.foodtech.back.util.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.foodtech.back.entity.model.OrderStatus.*;

@Service
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private WowKitchenBot wowKitchenBot;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final UserServiceImpl userService;

    private final ProductService productService;

    private final BonusService bonusService;

    private final RabbitMqService rabbitMqService;

    private final ResourcesProperties resourcesProperties;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            UserServiceImpl userService, ProductService productService, BonusService bonusService,
                            RabbitMqService rabbitMqService, ResourcesProperties resourcesProperties) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
        this.productService = productService;
        this.bonusService = bonusService;
        this.rabbitMqService = rabbitMqService;
        this.resourcesProperties = resourcesProperties;
    }

    @Override
    public Page<Order> getAllForAdmin(PageRequest pageRequest) {
        return orderRepository.findAll(pageRequest);
    }

    @Override
    public List<OrderItem> getItemsByOrderIdForAdmin(Long orderId) {
        return orderItemRepository.findAllByOrderId(orderId);
    }

    @Override
    public List<Order> getAllByUserWithItemsAndAddress(User user) {
        return orderRepository.findAllByUserFetchProductsAndAddress(user, forOrderHistory());
    }

    @Override
    public Optional<Order> getByIdAndUserWithItemsAndAddress(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserIdFetchProductsAndAddress(orderId, userId);
    }

    @Override
    public OrderStatus getStatusByIdAndUserId(Long orderId, Long userId) {
        return orderRepository.findStatusByIdAndUserId(orderId, userId).orElseThrow();
    }

    @Override
    public List<Order> getForStatusChecking() {
        return orderRepository.findByCheckStatusTrue();
    }

    @Override
    public Optional<Order> getUserOrderInProcessing(Long userId) {
        return orderRepository.findFirstByUserIdAndInProcessingIsTrueOrderByCreatedDesc(userId);
    }

    @Override
    public List<Long> getPaymentConfirmRequiredIds() {
        return orderRepository.findPaymentCompleteRequiredIds(forPaymentCompletion());
    }

    @Override
    public List<Long> getPaymentCancelRequiredIds() {
        return orderRepository.findPaymentCompleteRequiredIds(forPaymentCancelling());
    }

    @Override
    public Order register(Long userId, OrderRegistrationDto orderDto) {
        User user = userService.getWithActualAddress(userId);
        Map<Product, Integer> productsQuantity = productService.getProductsQuantity(orderDto.getItemsQuantity());
        if (productsQuantity.size() != orderDto.getItemsQuantity().size()) {
            throw new CartInvalidException("Some of product ID not found in database");
        }

        int costWithoutBonuses = countCartFullPrice(productsQuantity);
        int costWithBonuses = bonusService.countCartCostWithBonuses(costWithoutBonuses, user.getBonusAccount().getBonusAmount());
        if (costWithBonuses != orderDto.getTotalCost()) {
            throw new CartInvalidException("Cart total cost invalid");
        }
        return formOrderAndSave(user, productsQuantity, costWithBonuses, costWithoutBonuses, orderDto.isCutlery());
    }

    private int countCartFullPrice(Map<Product, Integer> productsWithAmount) {
        return productsWithAmount.entrySet().stream().mapToInt(p -> p.getKey().getPrice() * p.getValue()).sum();
    }

    private Order formOrderAndSave(User user, Map<Product, Integer> productsQuantity, Integer totalCost,
                                   Integer productsCost, boolean cutlery) {
        long orderId = System.currentTimeMillis();
        Order newOrder = new Order();
        newOrder.setId(orderId);
        newOrder.setUser(user);
        newOrder.setAddress(user.getActualAddress());
        newOrder.setTotalCost(totalCost);
        newOrder.setProductsCost(productsCost);
        newOrder.setAppliedBonusAmount(productsCost - totalCost);
        newOrder.setStatus(NEW);
        newOrder.setCutlery(cutlery);
        newOrder.setStatusQueueName(rabbitMqService.createQueue(RabbitMqQueueType.ORDER_STATUS_QUEUE, String.valueOf(orderId)));
        newOrder.setDeliveryTime(resourcesProperties.getDefaultDeliveryTime());
        newOrder.setItems(OrderMapper.toOrderItems(newOrder, productsQuantity));
        Order savedOrder = orderRepository.save(newOrder);
        String orderMessage = getOrderMessage(savedOrder);
        try {
            wowKitchenBot.sendMessageToChat(orderMessage);
        }catch (Exception e) {}
        return savedOrder;
    }

    private String getOrderMessage(Order order) {
        return  "Заказ #" + order.getId() + "\n" +
                "Имя клиента : " + order.getUser().getName() + "\n" +
                "Телефон клиента : " + order.getUser().getMobileNumber() + "\n" +
                "Сумма заказа : " + order.getTotalCost() + " тнг" + "\n" +
                "--Состав заказа : " + "\n" +
                itemsToText(order.getItems()) + "\n" +
                "Адрес : " + getAddress(order.getAddress());
    }

    private String itemsToText(List<OrderItem> items) {
        StringBuilder stringBuilder = new StringBuilder();
        items.forEach(orderItem -> {
            stringBuilder.append("--Название : ").append(orderItem.getProduct().getName()).append(" Кол-во :").append(orderItem.getAmount()).append("\n");
        });
        return stringBuilder.toString();
    }

    private String getAddress(Address address) {
        return address.getCity() + " " + address.getHome();
    }

    @Override
    public void processPaid(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(ON_SENDING);
        order.setCheckStatus(false); // пока заказ не отправится в айку убираем его с проверки статуса, чтобы избежать ситуации с повторной отправкой одного заказа
        orderRepository.save(order);
    }

    @Override
    public void processIikoSendingSuccess(Long orderId, IikoOrderSendingResult sendingResult) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        log.info("Order successfully sent to iiko. Order: '{}'. User: '{}'", order.getId(), order.getUser());
        order.setStatus(SENT);
        order.setIikoOrderId(sendingResult.getOrderInfo().getOrderId());
        order.setIikoShortId(sendingResult.getOrderInfo().getNumber());
        order.setIikoProblem(sendingResult.getProblem());
        order.setCheckStatus(true);
        orderRepository.save(order);
    }

    @Override
    public void processIikoSendingFail(Long orderId, IikoOrderSendingResult sendingResult) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        log.error("Order sending to iiko failed. Order: '{}'. User: '{}'", order.getId(), order.getUser());
        order.setStatus(SENDING_FAILED);
        order.setIikoProblem(sendingResult.getProblem());
        order.setInProcessing(false);
        order.setCheckStatus(false);
        order.getCloudPayment().setPaymentCompleteRequired(true);
        orderRepository.save(order);
    }

    @Override
    public void processInProgress(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(IN_PROGRESS);
        orderRepository.save(order);
    }

    @Override
    public void processReady(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(READY);
        orderRepository.save(order);
    }

    @Override
    public void processAwaitingDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(AWAITING_DELIVERY);
        orderRepository.save(order);
    }

    @Override
    public void processOnTheWay(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(ON_THE_WAY);
        orderRepository.save(order);
    }

    @Override
    public void processDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(DELIVERED);
        order.setInProcessing(false);
        order.getCloudPayment().setPaymentCompleteRequired(true);
        orderRepository.save(order);
    }

    @Override
    public void processClosed(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.getCloudPayment().setPaymentCompleteRequired(DELIVERED != order.getStatus());
        order.setStatus(CLOSED);
        order.setCheckStatus(false);
        order.setInProcessing(false);
        orderRepository.save(order);
    }

    @Override
    public void processCancelled(Long orderId) {
//        Order order = orderRepository.findById(orderId).orElseThrow();
//        order.setStatus(CANCELLED);
//        order.setCheckStatus(false);
//        order.setInProcessing(false);
//        order.getCloudPayment().setPaymentCompleteRequired(true);
//        orderRepository.save(order);
    }

    @Override
    public void processNotConfirmed(Long orderId) {
//        Order order = orderRepository.findById(orderId).orElseThrow();
//        order.setStatus(CANCELLED);
//        order.setCheckStatus(false);
//        order.setInProcessing(false);
//        order.getCloudPayment().setPaymentCompleteRequired(true);
//        orderRepository.save(order);
    }

    @Override
    public void processNotProcessed(Long orderId) {
//        Order order = orderRepository.findById(orderId).orElseThrow();
//        log.info("Order wasn't processed by iiko. Order: '{}'. User: '{}'", order.getId(), order.getUser());
//        order.setCheckStatus(false);
//        order.setInProcessing(false);
//        orderRepository.save(order);
    }
}

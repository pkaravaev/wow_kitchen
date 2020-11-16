package com.foodtech.back.service.iiko;

import com.foodtech.back.dto.iiko.IikoOrderInfo;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.OrderStatus;
import com.foodtech.back.service.event.*;
import com.foodtech.back.service.http.IikoHttpSender;
import com.foodtech.back.service.model.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.foodtech.back.dto.iiko.IikoOrderSendingResult.failed;
import static com.foodtech.back.entity.model.OrderStatus.*;
import static java.util.Objects.isNull;

@SuppressWarnings("rawtypes")
@Service
@Slf4j
@Profile({"prod", "dev"})
public class IikoOrderStatusCheckExecutor {

//    private static final long THREE_MINUTES = 180_000L;
    private static final long ONE_MINUTE = 60_000L;
    private static final long TIME_FOR_ORDER_SENDING_MINUTES = 15L;

    private final ThreadPoolTaskExecutor taskExecutor;

    private final OrderService orderService;

    private final ApplicationEventPublisher publisher;

    private final IikoHttpSender httpSender;

    public IikoOrderStatusCheckExecutor(@Qualifier(value = "statusCheckersExecutor") ThreadPoolTaskExecutor taskExecutor,
                                        OrderService orderService, ApplicationEventPublisher publisher,
                                        IikoHttpSender httpSender) {
        this.taskExecutor = taskExecutor;
        this.orderService = orderService;
        this.publisher = publisher;
        this.httpSender = httpSender;
    }

    @Scheduled(fixedDelay = ONE_MINUTE)
    @Transactional
    public void checkStatuses() {
        List<Order> orders = orderService.getForStatusChecking();
        log.debug("Start checking iiko orders statuses. There are {} iiko orders in processing", orders.size());
        List<IikoStatusCheckerTask> checkerTasks = formTasks(orders);
        CompletableFuture[] futures = execute(checkerTasks);
        CompletableFuture.allOf(futures).join(); // Дожидаемся окончания всех тасок, чтобы гарантировать, что следующая проверка не начнется до завершения предыдущей
    }

    private List<IikoStatusCheckerTask> formTasks(List<Order> ordersToCheck) {
        return ordersToCheck
                .stream()
                .map(IikoStatusCheckerTask::new)
                .collect(Collectors.toList());
    }

    private CompletableFuture[] execute(List<IikoStatusCheckerTask> tasks) {
        return tasks
                .stream()
                .map(task -> CompletableFuture.runAsync(task, taskExecutor))
                .toArray(CompletableFuture[]::new);
    }

    public class IikoStatusCheckerTask implements Runnable {

        private static final long CHECK_TIME_HOURS = 3;
        private final Order order;

        public IikoStatusCheckerTask(Order order) {
            this.order = order;
        }

        @Override
        public void run() {

            if (PAID.equals(order.getStatus())) {
                handlePaidStatus();
                return;
            }

            IikoOrderInfo orderInfo = httpSender.sendGetOrderInfo(order.getIikoOrderId());

            if (isNull(orderInfo) || !StringUtils.hasText(orderInfo.getStatus())) {
                log.error("Order '{}' info request sending failed", order.getId());
                return;
            }

            OrderStatus newStatus = parseIikoStatus(orderInfo.getStatus());
            OrderStatus prevStatus = order.getStatus();
            boolean statusChanged = newStatus != prevStatus;

            if (statusChanged) {
                publishEvent(newStatus);
                return;
            }

            checkTimeExpired(order.getCreated());
        }

        private void handlePaidStatus() {
            boolean timeToSendOrderExpired = order.getPaidTime().plusMinutes(TIME_FOR_ORDER_SENDING_MINUTES).isBefore(LocalDateTime.now());
            if (timeToSendOrderExpired) {
                publishEvent(SENDING_FAILED);
                return;
            }

            publishEvent(PAID);
        }

        private void checkTimeExpired(LocalDateTime startTime) {
            boolean expired = startTime.plusHours(CHECK_TIME_HOURS).isBefore(LocalDateTime.now());
            if (expired) {
                publishEvent(NOT_PROCESSED);
            }
        }

        private void publishEvent(OrderStatus newStatus) {
            log.info("Order status changed. Order: '{}'. New status: '{}'", order, newStatus);
            switch (newStatus) {
                case SENDING_FAILED:
                    publisher.publishEvent(new OrderSendingFailedEvent(order, failed("Paid order was not sent to iiko in 15 minutes")));
                    break;
                case PAID:
                    publisher.publishEvent(new OrderPaidEvent(order));
                    break;
                case IN_PROGRESS:
                    publisher.publishEvent(new OrderInProgressEvent(order));
                    break;
                case READY:
                    publisher.publishEvent(new OrderReadyEvent(order));
                    break;
                case AWAITING_DELIVERY:
                    publisher.publishEvent(new OrderAwaitingDeliveryEvent(order));
                    break;
                case ON_THE_WAY:
                    publisher.publishEvent(new OrderOnTheWayEvent(order));
                    break;
                case DELIVERED:
                    publisher.publishEvent(new OrderDeliveredEvent(order));
                    break;
                case CLOSED:
                    publisher.publishEvent(new OrderClosedEvent(order));
                    break;
                case NOT_CONFIRMED:
                    publisher.publishEvent(new OrderNotConfirmedEvent(order));
                    break;
                case CANCELLED:
                    publisher.publishEvent(new OrderCancelledEvent(order));
                    break;
                case NOT_PROCESSED:
                    publisher.publishEvent(new OrderNotProcessedEvent(order));
                    break;
                default:
                    throw new IllegalArgumentException("No event found for status: " + newStatus);
            }
        }
    }
}

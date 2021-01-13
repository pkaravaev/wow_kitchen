package com.foodtech.back.service.payment.cloud;

import com.foodtech.back.service.model.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@Profile({"prod", "dev"})
public class CloudPaymentScheduleService {

    private static final long ONE_MINUTE = 60_000L;

    private final OrderService orderService;

    private final CloudPaymentService paymentService;

    private final ThreadPoolTaskExecutor executor;

    public CloudPaymentScheduleService(OrderService orderService, CloudPaymentService paymentService,
                                       @Qualifier(value = "paymentCompletionExecutor")ThreadPoolTaskExecutor executor) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.executor = executor;
    }

    @Scheduled(fixedDelay = ONE_MINUTE)
    @Transactional
    public void checkPaymentsNeedToBeCompleted() {
        List<Long> ordersToConfirmPayment = orderService.getPaymentConfirmRequiredIds();
        List<Long> ordersToCancelPayment = orderService.getPaymentCancelRequiredIds();
        List<Runnable> tasks = formTasks(ordersToConfirmPayment, ordersToCancelPayment);
        CompletableFuture[] futures = execute(tasks);
        CompletableFuture.allOf(futures).join(); // Дожидаемся окончания всех тасок, чтобы гарантировать, что следующая проверка не начнется до завершения предыдущей
    }

    private List<Runnable> formTasks(List<Long> confirmIds, List<Long> cancelIds) {
        List<Runnable> toConfirm = confirmIds
                .stream()
                .map(PaymentConfirmTask::new)
                .collect(Collectors.toList());

//        List<Runnable> toCancel = cancelIds
//                .stream()
//                .map(PaymentCancelTask::new)
//                .collect(Collectors.toList());

//        toConfirm.addAll(toCancel);
        return toConfirm;
    }

    private CompletableFuture[] execute(List<Runnable> tasks) {
        return tasks
                .stream()
                .map(task -> CompletableFuture.runAsync(task, executor))
                .toArray(CompletableFuture[]::new);
    }

    class PaymentConfirmTask implements Runnable {
        private Long orderId;

        PaymentConfirmTask(Long orderId) {
            this.orderId = orderId;
        }

        @Override
        public void run() {
            paymentService.confirmAuth(orderId);
        }
    }

    class PaymentCancelTask implements Runnable {
        private Long orderId;

        PaymentCancelTask(Long orderId) {
            this.orderId = orderId;
        }

        @Override
        public void run() {
            paymentService.cancelAuth(orderId);
        }
    }
}

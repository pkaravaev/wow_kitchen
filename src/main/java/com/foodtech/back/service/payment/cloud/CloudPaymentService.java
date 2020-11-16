package com.foodtech.back.service.payment.cloud;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.payment.cloud.CloudPaymentBaseResponse;
import com.foodtech.back.dto.payment.cloud.CloudPaymentRequest;
import com.foodtech.back.dto.payment.cloud.CloudPaymentResponse;
import com.foodtech.back.dto.payment.cloud.CloudPaymentTransactionResultDto;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.OrderStatus;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.entity.payment.PaymentType;
import com.foodtech.back.entity.payment.cloud.CloudPayment;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.service.bonus.BonusService;
import com.foodtech.back.service.event.BonusCollisionEvent;
import com.foodtech.back.service.event.OrderNotPaidEvent;
import com.foodtech.back.service.http.CloudPaymentRequestSender;
import com.foodtech.back.service.model.BankCardService;
import com.foodtech.back.service.notification.ampq.RabbitMqService;
import com.foodtech.back.util.exceptions.CloudPaymentPayTransactionDeclinedException;
import com.foodtech.back.util.exceptions.PaymentRequestInvalidException;
import com.foodtech.back.util.exceptions.PaymentRequestSendingFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static com.foodtech.back.entity.model.OrderStatus.*;
import static com.foodtech.back.entity.payment.PaymentType.CARD;
import static com.foodtech.back.entity.payment.PaymentType.GOOGLE_PAY;
import static com.foodtech.back.entity.payment.cloud.CloudPaymentStatus.Cancelled;
import static com.foodtech.back.entity.payment.cloud.CloudPaymentStatus.Completed;
import static com.foodtech.back.service.notification.ampq.RabbitMqQueueType.PAYMENT_QUEUE;
import static java.util.Objects.isNull;

@Service
@Slf4j
@Transactional(noRollbackFor = {CloudPaymentPayTransactionDeclinedException.class})
public class CloudPaymentService {

    public static final Set<OrderStatus> NOT_PAID_STATUSES = Set.of(NEW, NOT_PAID, PAYMENT_FAILED, PAYMENT_DECLINED);

    private final CloudPaymentRequestSender requestSender;

    private final CloudPaymentRequestBuilder requestBuilder;

    private final BankCardService bankCardService;

    private final BonusService bonusService;

    private final OrderRepository orderRepository;

    private final RabbitMqService rabbitMqService;

    private final ResourcesProperties properties;

    private final ApplicationEventPublisher publisher;

    public CloudPaymentService(CloudPaymentRequestSender requestSender, CloudPaymentRequestBuilder requestBuilder,
                               BankCardService bankCardService, BonusService bonusService, OrderRepository orderRepository,
                               RabbitMqService rabbitMqService,
                               ResourcesProperties properties, ApplicationEventPublisher publisher) {
        this.requestSender = requestSender;
        this.requestBuilder = requestBuilder;
        this.bankCardService = bankCardService;
        this.bonusService = bonusService;
        this.orderRepository = orderRepository;
        this.rabbitMqService = rabbitMqService;
        this.properties = properties;
        this.publisher = publisher;
    }

    public void authWithCardToken(User user, Long orderId) {
        Order order = orderRepository.findByIdAndUserIdAndStatusIn(orderId, user.getId(), NOT_PAID_STATUSES).orElseThrow();
        BankCard bankCard = bankCardService.getActualCard(user.getId()).orElseThrow();

        CloudPaymentRequest request = requestBuilder.authRequest(user.getId(), bankCard.getToken(), order.getTotalCost(), order.getId());
        CloudPaymentBaseResponse response = requestSender.sendPaymentRequest(request);

        checkForRequestErrors(response);
        processSuccessfulRequest(order, response.getModel(), bankCard, CARD);
        checkTransactionApproved(response, order);
        writeOffBonuses(order);
        processApprovedTransaction(order);
    }

    public CloudPaymentTransactionResultDto authWithGooglePay(User user, Long orderId, String googlePaymentData) {

        Order order = orderRepository.findByIdAndUserIdAndStatusIn(orderId, user.getId(), NOT_PAID_STATUSES).orElseThrow();

        CloudPaymentRequest request = requestBuilder.authGooglePayRequest(user.getId(), googlePaymentData, order.getTotalCost(), order.getId());
        CloudPaymentBaseResponse response = requestSender.sendPaymentRequest(request);

        checkForRequestErrors(response);
        processSuccessfulRequest(order, response.getModel(), null, GOOGLE_PAY);
        checkTransactionApproved(response, order);

        if (response.isNeed3DSResponse()) {
            return processNeed3DSResponse(order, response.getModel());
        }

        processApprovedTransaction(order);
        return CloudPaymentTransactionResultDto
                .builder()
                .need3DS(false)
                .build();
    }

    private void checkForRequestErrors(CloudPaymentBaseResponse response) {
        if (isNull(response)) {
            throw new PaymentRequestSendingFailedException();
        }

        if (isNull(response.getModel()) && !response.isSuccess()) {
            throw new PaymentRequestInvalidException(response.getMessage());
        }
    }

    private void processSuccessfulRequest(Order order, CloudPaymentResponse model, BankCard bankCard, PaymentType type) {
        order.setCloudPayment(CloudPayment.fromResponseModel(model));
        order.setBankCard(bankCard);
        order.setPaymentType(type);
    }

    private void checkTransactionApproved(CloudPaymentBaseResponse response, Order order) {
        if (response.transactionDeclined()) {
            order.setStatus(NOT_PAID);
            throw new CloudPaymentPayTransactionDeclinedException(order.getId(), response.getModel().getCardHolderMessage());
        }
    }

    private void writeOffBonuses(Order order) {
        /* После оплаты списываем бонусы, проверяем, что они уже не были использованы в паралельном заказе */
        boolean writtenOffSuccessfully = bonusService.writeOffBonuses(order.getUser().getId(), order.getAppliedBonusAmount());
        if (!writtenOffSuccessfully) {
            order.setStatus(BONUS_COLLISION);
            order.getCloudPayment().setPaymentCompleteRequired(true);
            publisher.publishEvent(new BonusCollisionEvent(order));
            throw new CloudPaymentPayTransactionDeclinedException(order.getId(), "Произошла коллизия при применении бонусов");
        }
    }

    private CloudPaymentTransactionResultDto processNeed3DSResponse(Order order, CloudPaymentResponse response) {
        String queueName = rabbitMqService.createQueue(PAYMENT_QUEUE, String.valueOf(response.getTransactionId()));
        order.getCloudPayment().setPaymentQueueName(queueName);
        return CloudPaymentTransactionResultDto
                .builder()
                .need3DS(true)
                .acsUrl(response.getAcsUrl())
                .paReq(response.getPaReq())
                .merchantData(response.getTransactionId().toString())
                .termUrl(properties.getPayment3DSCallbackUrl())
                .amqpHost(properties.getRabbitMqHost())
                .queueName(queueName)
                .build();
    }

    private void processApprovedTransaction(Order order) {
        order.setStatus(PAID);
        order.setPaidTime(LocalDateTime.now());
        order.setInProcessing(true);
        order.setCheckStatus(true);
    }

    /* Обработка рез-та авторизации по 3DS (пока такой вариант возможен только при оплате через GooglePay)*/
    public void completePaymentAfter3DSCallback(String paRes, String merchantData) {
        Order order = orderRepository.findByCloudPaymentTransactionIdFetchUser(Long.parseLong(merchantData)).orElseThrow();
        /* Отправляем в платежный шлюз информацию PaRes для завершения процедуры 3DS авторизации */
        CloudPaymentRequest request = requestBuilder.paResRequest(Long.valueOf(merchantData), paRes);
        CloudPaymentBaseResponse response = requestSender.sendPaResRequest(request);
        processPayment3DSCallbackResult(order, response);
    }

    public static final String BAD_PaRes_REQUEST = "Bad PaRes request";

    private void processPayment3DSCallbackResult(Order order, CloudPaymentBaseResponse response) {
        if (isNull(response) || isNull(response.getModel())) {
            order.getCloudPayment().setDeclineReason(BAD_PaRes_REQUEST);
            publisher.publishEvent(new OrderNotPaidEvent(order));
            log.error("Payment failed. Order: '{}'. User: '{}'. Cause: 'Empty response'", order.getId(), order.getUser());

        } else if (!response.isSuccess()) {
            order.getCloudPayment().setDeclineReason(response.getModel().getReason());
            publisher.publishEvent(new OrderNotPaidEvent(order));
            log.info("Payment failed. Order: '{}'. User: '{}'. Cause: {}", order.getId(), order.getUser(), response.getModel().getCardHolderMessage());

        } else if (response.transactionApproved()){
            order.getCloudPayment().setCloudPaymentStatus(response.getModel().getStatus());
            order.setStatus(PAID);
            order.setPaidTime(LocalDateTime.now());
            order.setInProcessing(true);
            log.info("Payment success. Order: '{}'. User: '{}'.", order.getId(), order.getUser());
        }
    }

    public void confirmAuth(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        CloudPaymentRequest request = requestBuilder.confirmAuthRequest(order.getCloudPayment().getTransactionId(), order.getTotalCost());
        CloudPaymentBaseResponse response = requestSender.sendAuthFinalizeRequest(request);

        if (isNull(response)) {
            return;
        }

        order.getCloudPayment().setPaymentCompleteRequired(false);
        if (response.isSuccess()) {
            order.getCloudPayment().setCloudPaymentStatus(Completed);
            log.info("Confirmed payment authorization. Order: '{}'. User: '{}'", order.getId(), order.getUser());
            return;
        }

        log.warn("Payment can't be confirmed. Cause: '{}'. Order: '{}'. User: '{}'", response.getMessage(),
                order, order.getUser());
    }

    public void cancelAuth(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        CloudPaymentRequest request = requestBuilder.cancelAuthRequest(order.getCloudPayment().getTransactionId());
        CloudPaymentBaseResponse response = requestSender.sendAuthFinalizeRequest(request);

        if (isNull(response)) {
            return;
        }

        order.getCloudPayment().setPaymentCompleteRequired(false);
        if (response.isSuccess()) {
            order.getCloudPayment().setCloudPaymentStatus(Cancelled);
            log.info("Cancelled payment authorization. Order: '{}'. User: '{}'", order.getId(), order.getUser());
            return;
        }

        log.warn("Payment can't be cancelled. Cause: '{}'. Order: '{}'. User: '{}'", response.getMessage(),
                    order, order.getUser());

    }

    public boolean testCloudPayment() {
        return requestSender.sendTestRequest().isSuccess();
    }

    public String testPaymentProperties() {
        return properties.getPaymentPropertiesTest();
    }
}

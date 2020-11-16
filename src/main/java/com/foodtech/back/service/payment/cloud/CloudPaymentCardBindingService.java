package com.foodtech.back.service.payment.cloud;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.payment.cloud.*;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.entity.payment.cloud.BankCardBindRequest;
import com.foodtech.back.repository.payment.BankCardBindRequestRepository;
import com.foodtech.back.service.event.card.CardBindDeclinedEvent;
import com.foodtech.back.service.event.card.CardBindFailedEvent;
import com.foodtech.back.service.event.card.CardBindSuccessEvent;
import com.foodtech.back.service.http.CloudPaymentRequestSender;
import com.foodtech.back.service.model.BankCardService;
import com.foodtech.back.service.notification.ampq.RabbitMqService;
import com.foodtech.back.util.ResponseCode;
import com.foodtech.back.util.exceptions.CardBindingRequestInvalidException;
import com.foodtech.back.util.exceptions.CardBindingRequestSendingFailedException;
import com.foodtech.back.util.exceptions.CloudPaymentCardBindingTransactionDeclinedException;
import com.foodtech.back.util.mapper.UserMapper;
import com.foodtech.back.service.notification.ampq.RabbitMqQueueType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;

@Service
@Slf4j
@Transactional
public class CloudPaymentCardBindingService {

    private final CloudPaymentRequestSender requestSender;

    private final CloudPaymentRequestBuilder requestBuilder;

    private final BankCardService bankCardService;

    private final BankCardBindRequestRepository cardBindRequestRepository;

    private final RabbitMqService rabbitMqService;

    private final ApplicationEventPublisher publisher;

    private final ResourcesProperties properties;

    public CloudPaymentCardBindingService(CloudPaymentRequestSender requestSender, CloudPaymentRequestBuilder requestBuilder,
                                          BankCardService bankCardService, BankCardBindRequestRepository cardBindRequestRepository,
                                          RabbitMqService rabbitMqService, ApplicationEventPublisher publisher, ResourcesProperties properties) {
        this.requestSender = requestSender;
        this.requestBuilder = requestBuilder;
        this.bankCardService = bankCardService;
        this.cardBindRequestRepository = cardBindRequestRepository;
        this.rabbitMqService = rabbitMqService;
        this.publisher = publisher;
        this.properties = properties;
    }

    public CloudPaymentTransactionResultDto bindOrForm3DSParams(User user, BindCardDto bindDto) {

        CloudPaymentRequest request = requestBuilder.bindCardRequest(user.getId(), bindDto.getCardCryptogramPacket());
        CloudPaymentBaseResponse response = requestSender.sendCardBindRequest(request);
        checkForRequestErrors(response);
        checkTransactionApproved(response);
        return response.isNeed3DSResponse() ? processNeed3DSResponse(user, response.getModel())
                : processDoNotNeed3DSResponse(user, response.getModel());
    }

    private void checkForRequestErrors(CloudPaymentBaseResponse response) {
        if (isNull(response)) {
            throw new CardBindingRequestSendingFailedException();
        }

        if (isNull(response.getModel()) && !response.isSuccess()) {
            throw new CardBindingRequestInvalidException(response.getMessage());
        }

    }

    private void checkTransactionApproved(CloudPaymentBaseResponse response) {
        if (response.transactionDeclined()) {
            throw new CloudPaymentCardBindingTransactionDeclinedException(response.getModel().getCardHolderMessage());
        }
    }

    private CloudPaymentTransactionResultDto processNeed3DSResponse(User user, CloudPaymentResponse response) {
        String queueName = rabbitMqService.createQueue(RabbitMqQueueType.CARD_BINDING_QUEUE, String.valueOf(response.getTransactionId()));
        saveBankCardBindRequest(user, response.getTransactionId(), queueName);
        return CloudPaymentTransactionResultDto
                .builder()
                .need3DS(true)
                .merchantData(response.getTransactionId().toString())
                .paReq(response.getPaReq())
                .acsUrl(response.getAcsUrl())
                .termUrl(properties.getCardBinding3DSCallbackUrl())
                .amqpHost(properties.getRabbitMqHost())
                .queueName(queueName)
                .build();
    }

    private void saveBankCardBindRequest(User user, Long transactionId, String queueName) {
        BankCardBindRequest bindRequest = new BankCardBindRequest();
        bindRequest.setUser(user);
        bindRequest.setTransactionId(transactionId);
        bindRequest.setQueueName(queueName);
        cardBindRequestRepository.save(bindRequest);
    }

    private CloudPaymentTransactionResultDto processDoNotNeed3DSResponse(User user, CloudPaymentResponse response) {
        BankCard newBankCard = bankCardService.add(UserMapper.toBankCard(user, response));
        cancelCardBindingMoneyAuth(response.getTransactionId());
        return CloudPaymentTransactionResultDto
                .builder()
                .bankCard(newBankCard)
                .build();
    }

    public void completeCardBindingAfter3DSCallback(String paRes, String merchantData) {
        BankCardBindRequest cardBindRequest = cardBindRequestRepository
                .findByTransactionIdEquals(Long.parseLong(merchantData)).orElseThrow();
        /* Отправляем в платежный шлюз информацию PaRes для завершения процедуры 3DS авторизации */
        CloudPaymentRequest request = requestBuilder.paResRequest(Long.valueOf(merchantData), paRes);
        CloudPaymentBaseResponse response = requestSender.sendPaResRequest(request);
        processPaResResult(cardBindRequest, response);
        cancelCardBindingMoneyAuth(Long.valueOf(merchantData));
    }

    private void processPaResResult(BankCardBindRequest cardBindRequest, CloudPaymentBaseResponse response) {
        if (isNull(response) || isNull(response.getModel())) {
            processPaResRequestFailed(cardBindRequest);

        } else if (!response.isSuccess()) {
            processCardBindingDeclined(cardBindRequest, response.getModel());

        } else {
            processCardBindingSuccess(cardBindRequest, response.getModel());
        }
    }

    private void processPaResRequestFailed(BankCardBindRequest bindRequest) {
        bindRequest.setResult("Bad response");
        publisher.publishEvent(new CardBindFailedEvent(bindRequest));
        log.error("Card binding after 3DS callback failed. Cause: 'Empty response on PaRes request. User: '{}'", bindRequest.getUser());
    }

    private void processCardBindingDeclined(BankCardBindRequest bindRequest, CloudPaymentResponse response) {
        bindRequest.setResult(response.getReason());
        publisher.publishEvent(new CardBindDeclinedEvent(bindRequest));
        log.info("Card binding after 3DS callback declined. Cause: '{}'. User: '{}'", response.getCardHolderMessage(), bindRequest.getUser());
    }

    private void processCardBindingSuccess(BankCardBindRequest bindRequest, CloudPaymentResponse response) {
        bankCardService.add(UserMapper.toBankCard(bindRequest.getUser(), response));
        bindRequest.setResult(ResponseCode.OK.toString());
        publisher.publishEvent(new CardBindSuccessEvent(bindRequest));
        log.info("New bank card for user '{}' successfully bind", bindRequest.getUser());
    }

    /* Возвращаем пользователю деньги, списанные во время привязки карты */
    private void cancelCardBindingMoneyAuth(Long transactionId) {
        CloudPaymentRequest cancelRequest = requestBuilder.cancelAuthRequest(transactionId);
        requestSender.sendAuthFinalizeRequest(cancelRequest);
    }

}

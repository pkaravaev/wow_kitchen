package com.foodtech.back.unit.service.payment.cloud;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.payment.cloud.*;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.entity.payment.cloud.BankCardBindRequest;
import com.foodtech.back.entity.payment.cloud.CloudPaymentStatus;
import com.foodtech.back.repository.payment.BankCardBindRequestRepository;
import com.foodtech.back.service.event.card.CardBindDeclinedEvent;
import com.foodtech.back.service.event.card.CardBindFailedEvent;
import com.foodtech.back.service.event.card.CardBindSuccessEvent;
import com.foodtech.back.service.http.CloudPaymentRequestSender;
import com.foodtech.back.service.model.BankCardServiceImpl;
import com.foodtech.back.service.notification.ampq.RabbitMqEventListeners;
import com.foodtech.back.service.notification.ampq.RabbitMqQueueType;
import com.foodtech.back.service.notification.ampq.RabbitMqService;
import com.foodtech.back.service.payment.cloud.CloudPaymentCardBindingService;
import com.foodtech.back.service.payment.cloud.CloudPaymentRequestBuilder;
import com.foodtech.back.unit.AbstractUnitTest;
import com.foodtech.back.util.ResponseCode;
import com.foodtech.back.util.exceptions.CardBindingRequestInvalidException;
import com.foodtech.back.util.exceptions.CardBindingRequestSendingFailedException;
import com.foodtech.back.util.exceptions.CloudPaymentCardBindingTransactionDeclinedException;
import com.foodtech.back.util.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CloudPaymentCardBindingServiceUnitTest extends AbstractUnitTest {

    @Autowired
    CloudPaymentCardBindingService cardBindingService;

    @MockBean
    CloudPaymentRequestSender requestSender;

    @MockBean
    CloudPaymentRequestBuilder requestBuilder;

    @MockBean
    BankCardServiceImpl bankCardService;

    @MockBean
    BankCardBindRequestRepository cardBindRequestRepository;

    @MockBean
    RabbitMqService rabbitMqService;

    @MockBean
    ApplicationEventPublisher publisher;

    @Autowired
    ResourcesProperties properties;

    @MockBean
    RabbitMqEventListeners rabbitMqEventListeners;

    @Test
    void bindNeed3DS() {
        User user = user();
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.bindCardRequest(USER_ID, CRYPTO)).thenReturn(request);
        CloudPaymentBaseResponse need3DSResponse = need3DSResponse();
        when(requestSender.sendCardBindRequest(request)).thenReturn(need3DSResponse);
        when(rabbitMqService.createQueue(RabbitMqQueueType.CARD_BINDING_QUEUE, String.valueOf(TRANSACTION_ID)))
                .thenReturn(QUEUE_NAME);

        CloudPaymentTransactionResultDto resultDto = cardBindingService.bindOrForm3DSParams(user, bindCardDto());

        assertTrue(resultDto.isNeed3DS());
        assertEquals(String.valueOf(TRANSACTION_ID), resultDto.getMerchantData());
        assertEquals(PA_REQ, resultDto.getPaReq());
        assertEquals(ACS, resultDto.getAcsUrl());
        assertEquals(properties.getCardBinding3DSCallbackUrl(), resultDto.getTermUrl());
        assertEquals(properties.getRabbitMqHost(), resultDto.getAmqpHost());
        assertEquals(QUEUE_NAME, resultDto.getQueueName());

        BankCardBindRequest cardBindRequest = cardBindRequestEntity();
        cardBindRequest.setUser(user);
        verify(cardBindRequestRepository, times(1)).save(cardBindRequest);
    }

    @Test
    void bindWithout3DS() {
        User user = user();
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.bindCardRequest(USER_ID, CRYPTO)).thenReturn(request);
        CloudPaymentBaseResponse approvedResponse = transactionApprovedResponse();
        when(requestSender.sendCardBindRequest(request)).thenReturn(approvedResponse);
        BankCard bankCard = bankCard();
        when(bankCardService.add(any(BankCard.class))).thenReturn(bankCard);

        CloudPaymentTransactionResultDto resultDto = cardBindingService.bindOrForm3DSParams(user, bindCardDto());

        assertFalse(resultDto.isNeed3DS());
        assertEquals(bankCard, resultDto.getBankCard());
        verify(cardBindRequestRepository, times(0)).save(any(BankCardBindRequest.class));
    }

    @Test
    void bindRequestFailed() {
        User user = user();
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.bindCardRequest(USER_ID, CRYPTO)).thenReturn(request);
        when(requestSender.sendCardBindRequest(request)).thenReturn(null);

        assertThrows(CardBindingRequestSendingFailedException.class, () -> cardBindingService.bindOrForm3DSParams(user, bindCardDto()));

        verify(cardBindRequestRepository, times(0)).save(any(BankCardBindRequest.class));
        verify(bankCardService, times(0)).add(any(BankCard.class));
    }

    @Test
    void bindRequestInvalid() {
        User user = user();
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.bindCardRequest(USER_ID, CRYPTO)).thenReturn(request);
        CloudPaymentBaseResponse response = invalidRequestResponse();
        when(requestSender.sendCardBindRequest(request)).thenReturn(response);

        assertThrows(CardBindingRequestInvalidException.class, () -> cardBindingService.bindOrForm3DSParams(user, bindCardDto()));

        verify(cardBindRequestRepository, times(0)).save(any(BankCardBindRequest.class));
        verify(bankCardService, times(0)).add(any(BankCard.class));
    }

    @Test
    void bindRequestDeclined() {
        User user = user();
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.bindCardRequest(USER_ID, CRYPTO)).thenReturn(request);
        CloudPaymentBaseResponse declinedResponse = transactionDeclinedResponse();
        when(requestSender.sendCardBindRequest(request)).thenReturn(declinedResponse);

        assertThrows(CloudPaymentCardBindingTransactionDeclinedException.class, () -> cardBindingService.bindOrForm3DSParams(user, bindCardDto()));

        verify(cardBindRequestRepository, times(0)).save(any(BankCardBindRequest.class));
        verify(bankCardService, times(0)).add(any(BankCard.class));
    }

    @Test
    void callback3DSApproved() {
        User user = user();
        BankCardBindRequest bindRequest = cardBindRequestEntity();
        bindRequest.setUser(user);
        when(cardBindRequestRepository.findByTransactionIdEquals(TRANSACTION_ID)).thenReturn(Optional.of(bindRequest));
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.paResRequest(TRANSACTION_ID, PA_RES)).thenReturn(request);
        CloudPaymentBaseResponse approvedResponse = transactionApprovedResponse();
        when(requestSender.sendPaResRequest(request)).thenReturn(approvedResponse);

        cardBindingService.completeCardBindingAfter3DSCallback(PA_RES, String.valueOf(TRANSACTION_ID));

        assertEquals(ResponseCode.OK.toString(), bindRequest.getResult());
        verify(bankCardService, times(1)).add(UserMapper.toBankCard(user, approvedResponse.getModel()));
        verify(rabbitMqEventListeners, times(1)).sendCardBindSuccessMsg(new CardBindSuccessEvent(bindRequest));
    }

    @Test
    void callbackNullResponse() {
        User user = user();
        BankCardBindRequest bindRequest = cardBindRequestEntity();
        bindRequest.setUser(user);
        when(cardBindRequestRepository.findByTransactionIdEquals(TRANSACTION_ID)).thenReturn(Optional.of(bindRequest));
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.paResRequest(TRANSACTION_ID, PA_RES)).thenReturn(request);
        when(requestSender.sendPaResRequest(request)).thenReturn(null);

        cardBindingService.completeCardBindingAfter3DSCallback(PA_RES, String.valueOf(TRANSACTION_ID));

        assertEquals("Bad response", bindRequest.getResult());
        verify(bankCardService, times(0)).add(any(BankCard.class));
        verify(rabbitMqEventListeners, times(1)).sendCardBindFailedMsg(new CardBindFailedEvent(bindRequest));
    }

    @Test
    void callbackNullModelResponse() {
        User user = user();
        BankCardBindRequest bindRequest = cardBindRequestEntity();
        bindRequest.setUser(user);
        when(cardBindRequestRepository.findByTransactionIdEquals(TRANSACTION_ID)).thenReturn(Optional.of(bindRequest));
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.paResRequest(TRANSACTION_ID, PA_RES)).thenReturn(request);
        when(requestSender.sendPaResRequest(request)).thenReturn(new CloudPaymentBaseResponse());

        cardBindingService.completeCardBindingAfter3DSCallback(PA_RES, String.valueOf(TRANSACTION_ID));

        assertEquals("Bad response", bindRequest.getResult());
        verify(bankCardService, times(0)).add(any(BankCard.class));
        verify(rabbitMqEventListeners, times(1)).sendCardBindFailedMsg(new CardBindFailedEvent(bindRequest));
    }

    @Test
    void callbackDeclinedResponse() {
        User user = user();
        BankCardBindRequest bindRequest = cardBindRequestEntity();
        bindRequest.setUser(user);
        when(cardBindRequestRepository.findByTransactionIdEquals(TRANSACTION_ID)).thenReturn(Optional.of(bindRequest));
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.paResRequest(TRANSACTION_ID, PA_RES)).thenReturn(request);
        CloudPaymentBaseResponse declinedResponse = transactionDeclinedResponse();
        when(requestSender.sendPaResRequest(request)).thenReturn(declinedResponse);

        cardBindingService.completeCardBindingAfter3DSCallback(PA_RES, String.valueOf(TRANSACTION_ID));

        assertEquals(declinedResponse.getModel().getReason(), bindRequest.getResult());
        verify(bankCardService, times(0)).add(any(BankCard.class));
        verify(rabbitMqEventListeners, times(1)).sendCardBindDeclinedMsg(new CardBindDeclinedEvent(bindRequest));
    }

    @Test
    void callbackRequestNotFound() {
        when(cardBindRequestRepository.findByTransactionIdEquals(TRANSACTION_ID)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> cardBindingService.completeCardBindingAfter3DSCallback(PA_RES, String.valueOf(TRANSACTION_ID)));

        verify(bankCardService, times(0)).add(any(BankCard.class));
    }

    private static Long USER_ID = 1L;
    private static Long TRANSACTION_ID = 1111L;
    private static String CRYPTO = "CARD_CRYPTOGRAM_PACKET";
    private static String QUEUE_NAME = "QUEUE_NAME";
    private static String ACS = "https://acs.ru";
    private static String PA_REQ = "PaReq";
    private static String PA_RES = "PaRes";

    private static User user() {
        User user = new User();
        user.setId(USER_ID);
        user.setCountryCode("7");
        user.setMobileNumber("7777777777");
        return user;
    }

    private BindCardDto bindCardDto() {
        BindCardDto dto = new BindCardDto();
        dto.setCardCryptogramPacket(CRYPTO);
        return dto;
    }

    private static CloudPaymentBaseResponse need3DSResponse() {
        CloudPaymentBaseResponse cloudResponse = new CloudPaymentBaseResponse();
        cloudResponse.setSuccess(false);
        CloudPaymentResponse cloudModel = new CloudPaymentResponse();
        cloudModel.setTransactionId(TRANSACTION_ID);
        cloudModel.setPaReq(PA_REQ);
        cloudModel.setAcsUrl(ACS);
        cloudResponse.setModel(cloudModel);
        return cloudResponse;
    }

    private static CloudPaymentBaseResponse transactionApprovedResponse() {
        CloudPaymentBaseResponse cloudResponse = new CloudPaymentBaseResponse();
        cloudResponse.setSuccess(true);
        CloudPaymentResponse cloudModel = new CloudPaymentResponse();
        cloudModel.setCardType("VISA");
        cloudModel.setCardFirstSix("666666");
        cloudModel.setCardLastFour("4444");
        cloudModel.setTransactionId(TRANSACTION_ID);
        cloudModel.setReasonCode(0);
        cloudModel.setReason("Approved");
        cloudModel.setToken("CARD_TOKEN");
        cloudModel.setStatus(CloudPaymentStatus.Authorized);
        cloudResponse.setModel(cloudModel);
        return cloudResponse;
    }

    private static CloudPaymentBaseResponse invalidRequestResponse() {
        CloudPaymentBaseResponse cloudResponse = new CloudPaymentBaseResponse();
        cloudResponse.setSuccess(false);
        return cloudResponse;
    }


    private static CloudPaymentBaseResponse transactionDeclinedResponse() {
        CloudPaymentBaseResponse cloudResponse = new CloudPaymentBaseResponse();
        cloudResponse.setSuccess(false);
        CloudPaymentResponse cloudModel = new CloudPaymentResponse();
        cloudModel.setTransactionId(TRANSACTION_ID);
        cloudModel.setStatus(CloudPaymentStatus.Declined);
        cloudModel.setReason("InsufficientFunds");
        cloudModel.setReasonCode(5);
        cloudModel.setCardHolderMessage("Недостаточно средств на карте");
        cloudResponse.setModel(cloudModel);
        return cloudResponse;
    }

    private static BankCardBindRequest cardBindRequestEntity() {
        BankCardBindRequest bindRequest = new BankCardBindRequest();
        bindRequest.setTransactionId(TRANSACTION_ID);
        bindRequest.setQueueName(QUEUE_NAME);
        return bindRequest;
    }

    private static BankCard bankCard() {
        BankCard bankCard = new BankCard();
        bankCard.setId(1L);
        bankCard.setToken("SOME_CARD_TOKEN");
        bankCard.setCardMask("777777******9999");
        bankCard.setCardType("VISA");
        bankCard.setCardIssuer("Iron Bank of Braavos");
        bankCard.setActual(true);
        return bankCard;
    }
}
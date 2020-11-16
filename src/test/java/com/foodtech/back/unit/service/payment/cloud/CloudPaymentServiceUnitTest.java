package com.foodtech.back.unit.service.payment.cloud;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.payment.cloud.CloudPaymentBaseResponse;
import com.foodtech.back.dto.payment.cloud.CloudPaymentRequest;
import com.foodtech.back.dto.payment.cloud.CloudPaymentResponse;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.entity.payment.PaymentType;
import com.foodtech.back.entity.payment.cloud.CloudPayment;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.service.bonus.BonusService;
import com.foodtech.back.service.http.CloudPaymentRequestSender;
import com.foodtech.back.service.model.BankCardServiceImpl;
import com.foodtech.back.service.notification.ampq.RabbitMqService;
import com.foodtech.back.service.notification.push.FirebasePushSender;
import com.foodtech.back.service.payment.cloud.CloudPaymentRequestBuilder;
import com.foodtech.back.service.payment.cloud.CloudPaymentService;
import com.foodtech.back.unit.AbstractUnitTest;
import com.foodtech.back.util.exceptions.CloudPaymentPayTransactionDeclinedException;
import com.foodtech.back.util.exceptions.PaymentRequestInvalidException;
import com.foodtech.back.util.exceptions.PaymentRequestSendingFailedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static com.foodtech.back.dto.payment.cloud.CloudPaymentRequestType.AUTH;
import static com.foodtech.back.entity.model.OrderStatus.*;
import static com.foodtech.back.entity.payment.cloud.CloudPaymentStatus.*;
import static com.foodtech.back.service.payment.cloud.CloudPaymentService.NOT_PAID_STATUSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CloudPaymentServiceUnitTest extends AbstractUnitTest {

    @Autowired
    CloudPaymentService paymentService;

    @Autowired
    ResourcesProperties properties;

    @MockBean
    CloudPaymentRequestSender requestSender;

    @MockBean
    CloudPaymentRequestBuilder requestBuilder;

    @MockBean
    BankCardServiceImpl bankCardService;

    @MockBean
    BonusService bonusService;

    @MockBean
    OrderRepository orderRepository;

    @MockBean
    RabbitMqService rabbitMqService;

    @MockBean
    FirebasePushSender pushSender;

    @Test
    void authWithToken() {
        Order order = order();
        User user = user();
        order.setUser(user);
        BankCard card = card();
        when(orderRepository.findByIdAndUserIdAndStatusIn(order.getId(), user.getId(), NOT_PAID_STATUSES))
                .thenReturn(Optional.of(order));
        when(bankCardService.getActualCard(user.getId())).thenReturn(Optional.of(card));
        CloudPaymentRequest request = request();
        when(requestBuilder.authRequest(user.getId(), card.getToken(), order.getTotalCost(), order.getId()))
                .thenReturn(request);
        CloudPaymentBaseResponse response = approvedResponse();
        when(requestSender.sendPaymentRequest(request)).thenReturn(response);
        when(bonusService.writeOffBonuses(user.getId(), order.getAppliedBonusAmount())).thenReturn(true);

        paymentService.authWithCardToken(user, order.getId());

        CloudPayment cloudPayment = order.getCloudPayment();
        assertNotNull(cloudPayment);
        assertEquals(response.getModel().getTransactionId(), cloudPayment.getTransactionId());
        assertEquals(response.getModel().getReasonCode(), cloudPayment.getDeclineReasonCode());
        assertEquals(response.getModel().getReason(), cloudPayment.getDeclineReason());
        assertEquals(response.getModel().getStatus(), cloudPayment.getCloudPaymentStatus());

        assertThat(order.getBankCard()).isEqualToComparingFieldByField(card);
        assertEquals(PaymentType.CARD, order.getPaymentType());
        assertEquals(PAID, order.getStatus());
        assertNotNull(order.getPaidTime());
        assertTrue(order.isInProcessing());
        assertTrue(order.isCheckStatus());
    }

    @Test
    void authWithTokenRequestFailed() {
        Order order = order();
        User user = user();
        order.setUser(user);
        BankCard card = card();
        when(orderRepository.findByIdAndUserIdAndStatusIn(order.getId(), user.getId(), NOT_PAID_STATUSES))
                .thenReturn(Optional.of(order));
        when(bankCardService.getActualCard(user.getId())).thenReturn(Optional.of(card));
        CloudPaymentRequest request = request();
        when(requestBuilder.authRequest(user.getId(), card.getToken(), order.getTotalCost(), order.getId()))
                .thenReturn(request);
        when(requestSender.sendPaymentRequest(request)).thenReturn(null);
        when(bonusService.writeOffBonuses(user.getId(), order.getAppliedBonusAmount())).thenReturn(true);

        assertThrows(PaymentRequestSendingFailedException.class, () -> paymentService.authWithCardToken(user, order.getId()));

        assertNull(order.getCloudPayment());
        assertEquals(NEW, order.getStatus());
        assertFalse(order.isInProcessing());
        assertFalse(order.isCheckStatus());
    }

    @Test
    void authWithTokenRequestInvalid() {
        Order order = order();
        User user = user();
        order.setUser(user);
        BankCard card = card();
        when(orderRepository.findByIdAndUserIdAndStatusIn(order.getId(), user.getId(), NOT_PAID_STATUSES))
                .thenReturn(Optional.of(order));
        when(bankCardService.getActualCard(user.getId())).thenReturn(Optional.of(card));
        CloudPaymentRequest request = request();
        when(requestBuilder.authRequest(user.getId(), card.getToken(), order.getTotalCost(), order.getId()))
                .thenReturn(request);
        when(requestSender.sendPaymentRequest(request)).thenReturn(invalidResponse());
        when(bonusService.writeOffBonuses(user.getId(), order.getAppliedBonusAmount())).thenReturn(true);

        assertThrows(PaymentRequestInvalidException.class, () -> paymentService.authWithCardToken(user, order.getId()));

        assertNull(order.getCloudPayment());
        assertEquals(NEW, order.getStatus());
        assertFalse(order.isInProcessing());
        assertFalse(order.isCheckStatus());
    }

    @Test
    void authWithTokenDeclined() {
        Order order = order();
        User user = user();
        order.setUser(user);
        BankCard card = card();
        when(orderRepository.findByIdAndUserIdAndStatusIn(order.getId(), user.getId(), NOT_PAID_STATUSES))
                .thenReturn(Optional.of(order));
        when(bankCardService.getActualCard(user.getId())).thenReturn(Optional.of(card));
        CloudPaymentRequest request = request();
        when(requestBuilder.authRequest(user.getId(), card.getToken(), order.getTotalCost(), order.getId()))
                .thenReturn(request);
        CloudPaymentBaseResponse response = declineResponse();
        when(requestSender.sendPaymentRequest(request)).thenReturn(response);
        when(bonusService.writeOffBonuses(user.getId(), order.getAppliedBonusAmount())).thenReturn(true);

        assertThrows(CloudPaymentPayTransactionDeclinedException.class, () -> paymentService.authWithCardToken(user, order.getId()));

        CloudPayment cloudPayment = order.getCloudPayment();
        assertNotNull(cloudPayment);
        assertEquals(response.getModel().getTransactionId(), cloudPayment.getTransactionId());
        assertEquals(response.getModel().getReasonCode(), cloudPayment.getDeclineReasonCode());
        assertEquals(response.getModel().getReason(), cloudPayment.getDeclineReason());
        assertEquals(response.getModel().getStatus(), cloudPayment.getCloudPaymentStatus());
        assertEquals(NOT_PAID, order.getStatus());
        assertFalse(order.isInProcessing());
        assertFalse(order.isCheckStatus());
    }

    @Test
    void authWithTokenBonusCollision() {
        Order order = order();
        User user = user();
        order.setUser(user);
        BankCard card = card();
        when(orderRepository.findByIdAndUserIdAndStatusIn(order.getId(), user.getId(), NOT_PAID_STATUSES))
                .thenReturn(Optional.of(order));
        when(bankCardService.getActualCard(user.getId())).thenReturn(Optional.of(card));
        CloudPaymentRequest request = request();
        when(requestBuilder.authRequest(user.getId(), card.getToken(), order.getTotalCost(), order.getId()))
                .thenReturn(request);
        CloudPaymentBaseResponse response = approvedResponse();
        when(requestSender.sendPaymentRequest(request)).thenReturn(response);
        when(bonusService.writeOffBonuses(user.getId(), order.getAppliedBonusAmount())).thenReturn(false);

        assertThrows(CloudPaymentPayTransactionDeclinedException.class, () -> paymentService.authWithCardToken(user, order.getId()));

        CloudPayment cloudPayment = order.getCloudPayment();
        assertNotNull(cloudPayment);
        assertEquals(response.getModel().getTransactionId(), cloudPayment.getTransactionId());
        assertEquals(response.getModel().getReasonCode(), cloudPayment.getDeclineReasonCode());
        assertEquals(response.getModel().getReason(), cloudPayment.getDeclineReason());
        assertEquals(response.getModel().getStatus(), cloudPayment.getCloudPaymentStatus());
        assertEquals(BONUS_COLLISION, order.getStatus());
        assertFalse(order.isInProcessing());
        assertFalse(order.isCheckStatus());
    }

    @Test
    void confirmAuth() {
        Order order = order();
        CloudPayment cloudPayment = cloudPayment();
        order.setCloudPayment(cloudPayment);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.confirmAuthRequest(cloudPayment.getTransactionId(), order.getTotalCost())).thenReturn(request);
        CloudPaymentBaseResponse response = new CloudPaymentBaseResponse();
        response.setSuccess(true);
        when(requestSender.sendAuthFinalizeRequest(request)).thenReturn(response);

        paymentService.confirmAuth(order.getId());

        assertEquals(Completed, cloudPayment.getCloudPaymentStatus());
        assertFalse(cloudPayment.isPaymentCompleteRequired());
    }

    @Test
    void confirmAuthRequestFailed() {
        Order order = order();
        CloudPayment cloudPayment = cloudPayment();
        order.setCloudPayment(cloudPayment);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.confirmAuthRequest(cloudPayment.getTransactionId(), order.getTotalCost())).thenReturn(request);
        when(requestSender.sendAuthFinalizeRequest(request)).thenReturn(null);

        paymentService.confirmAuth(order.getId());

        assertNotEquals(Completed, cloudPayment.getCloudPaymentStatus());
        assertTrue(cloudPayment.isPaymentCompleteRequired());
    }

    @Test
    void confirmAuthRequestDeclined() {
        Order order = order();
        CloudPayment cloudPayment = cloudPayment();
        order.setCloudPayment(cloudPayment);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.confirmAuthRequest(cloudPayment.getTransactionId(), order.getTotalCost())).thenReturn(request);
        CloudPaymentBaseResponse response = new CloudPaymentBaseResponse();
        response.setSuccess(false);
        when(requestSender.sendAuthFinalizeRequest(request)).thenReturn(response);

        paymentService.confirmAuth(order.getId());

        assertNotEquals(Completed, cloudPayment.getCloudPaymentStatus());
        assertFalse(cloudPayment.isPaymentCompleteRequired());
    }

    @Test
    void cancelAuth() {
        Order order = order();
        CloudPayment cloudPayment = cloudPayment();
        order.setCloudPayment(cloudPayment);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.cancelAuthRequest(cloudPayment.getTransactionId())).thenReturn(request);
        CloudPaymentBaseResponse response = new CloudPaymentBaseResponse();
        response.setSuccess(true);
        when(requestSender.sendAuthFinalizeRequest(request)).thenReturn(response);

        paymentService.cancelAuth(order.getId());

        assertEquals(Cancelled, cloudPayment.getCloudPaymentStatus());
        assertFalse(cloudPayment.isPaymentCompleteRequired());
    }

    @Test
    void cancelAuthRequestFailed() {
        Order order = order();
        CloudPayment cloudPayment = cloudPayment();
        order.setCloudPayment(cloudPayment);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.cancelAuthRequest(cloudPayment.getTransactionId())).thenReturn(request);
        when(requestSender.sendAuthFinalizeRequest(request)).thenReturn(null);

        paymentService.cancelAuth(order.getId());

        assertNotEquals(Cancelled, cloudPayment.getCloudPaymentStatus());
        assertTrue(cloudPayment.isPaymentCompleteRequired());
    }

    @Test
    void cancelAuthRequestDeclined() {
        Order order = order();
        CloudPayment cloudPayment = cloudPayment();
        order.setCloudPayment(cloudPayment);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        CloudPaymentRequest request = CloudPaymentRequest.builder().build();
        when(requestBuilder.cancelAuthRequest(cloudPayment.getTransactionId())).thenReturn(request);
        CloudPaymentBaseResponse response = new CloudPaymentBaseResponse();
        response.setSuccess(false);
        when(requestSender.sendAuthFinalizeRequest(request)).thenReturn(response);

        paymentService.cancelAuth(order.getId());

        assertNotEquals(Cancelled, cloudPayment.getCloudPaymentStatus());
        assertFalse(cloudPayment.isPaymentCompleteRequired());
    }


    private static Long USER_ID = 1L;
    private static Long ORDER_ID = 1L;
    private static Integer ORDER_TOTAL_COST = 300;
    private static Long BANK_CARD_ID = 1L;
    private static String BANK_CARD_TOKEN = "CARD_TOKEN";


    private static User user() {
        User user = new User();
        user.setId(USER_ID);
        user.setCountryCode("7");
        user.setMobileNumber("7777777777");
        return user;
    }

    private static Order order() {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setStatus(NEW);
        order.setTotalCost(ORDER_TOTAL_COST);
        order.setAppliedBonusAmount(100);
        return order;
    }

    private static CloudPayment cloudPayment() {
        CloudPayment cloudPayment = new CloudPayment();
        cloudPayment.setCloudPaymentStatus(Authorized);
        cloudPayment.setTransactionId(1111L);
        cloudPayment.setPaymentCompleteRequired(true);
        return cloudPayment;
    }

    private static BankCard card() {
        BankCard card = new BankCard();
        card.setId(BANK_CARD_ID);
        card.setToken(BANK_CARD_TOKEN);
        card.setActual(true);
        card.setCardIssuer("Iron Bank Of Braavos");
        card.setCardMask("666666******4444");
        card.setCardType("VISA");
        return card;
    }

    private static CloudPaymentBaseResponse approvedResponse() {
        CloudPaymentBaseResponse cloudResponse = new CloudPaymentBaseResponse();
        cloudResponse.setSuccess(true);
        CloudPaymentResponse cloudModel = new CloudPaymentResponse();
        cloudModel.setCardType("VISA");
        cloudModel.setCardFirstSix("666666");
        cloudModel.setCardLastFour("4444");
        cloudModel.setTransactionId(1111L);
        cloudModel.setReasonCode(0);
        cloudModel.setReason("Approved");
        cloudModel.setToken("CARD_TOKEN");
        cloudModel.setStatus(Authorized);
        cloudResponse.setModel(cloudModel);
        return cloudResponse;
    }

    private static CloudPaymentBaseResponse invalidResponse() {
        CloudPaymentBaseResponse cloudResponse = new CloudPaymentBaseResponse();
        cloudResponse.setSuccess(false);
        return cloudResponse;
    }

    private static CloudPaymentBaseResponse declineResponse() {
        CloudPaymentBaseResponse cloudResponse = new CloudPaymentBaseResponse();
        cloudResponse.setSuccess(false);
        CloudPaymentResponse cloudModel = new CloudPaymentResponse();
        cloudModel.setTransactionId(1111L);
        cloudModel.setStatus(Declined);
        cloudModel.setReason("InsufficientFunds");
        cloudModel.setReasonCode(5);
        cloudModel.setCardHolderMessage("Недостаточно средств на карте");
        cloudResponse.setModel(cloudModel);
        return cloudResponse;
    }

    private static CloudPaymentRequest request() {
        return CloudPaymentRequest.builder()
                .type(AUTH)
                .amount(ORDER_TOTAL_COST)
                .token(BANK_CARD_TOKEN)
                .invoiceId(ORDER_ID.toString())
                .accountId(USER_ID.toString())
                .currency("RUB")
                .description("Оплата заказа")
                .build();
    }


}
package com.foodtech.back.integration.controller;

import com.foodtech.back.dto.payment.cloud.CloudPaymentRequest;
import com.foodtech.back.dto.payment.cloud.CloudPaymentResponse;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.payment.cloud.CloudPayment;
import com.foodtech.back.entity.payment.cloud.CloudPaymentStatus;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.service.event.BonusCollisionEvent;
import com.foodtech.back.service.http.CloudPaymentRequestSender;
import com.foodtech.back.service.notification.ampq.RabbitMqService;
import com.foodtech.back.service.notification.push.FirebasePushEventListeners;
import com.foodtech.back.util.DateUtil;
import com.foodtech.back.util.ResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static com.foodtech.back.IntegrationTestData.KITCHEN_OPEN_TIME;
import static com.foodtech.back.entity.model.OrderStatus.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"classpath:delete-payment-test-data.sql", "classpath:delete-product-test-data.sql",
        "classpath:user-data.sql", "classpath:product-test-data.sql", "classpath:payment-test-data.sql"})
@Sql(scripts = {"classpath:delete-payment-test-data.sql", "classpath:delete-product-test-data.sql"}, executionPhase = AFTER_TEST_METHOD)
class CloudPaymentIntegrationControllerTest extends AbstractControllerIntegrationTest {

    @MockBean
    public CloudPaymentRequestSender paymentRequestSender;

    @MockBean
    public RabbitMqService rabbitMqService;

    @MockBean
    public FirebasePushEventListeners pushEventListeners;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private DateUtil dateUtil;

    @BeforeEach
    void setUp() {
        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_OPEN_TIME);
    }

    @Test
    void payWithCardTokenSuccess() throws Exception {
        when(paymentRequestSender.sendPaymentRequest(any(CloudPaymentRequest.class))).thenReturn(testData.transactionApprovedResponse());

        mockMvc.perform(post("/app/order/payment")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.order4_PaymentDto())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ResponseCode.OK.toString()));

        assertOrderPaidSavedCorrectly();
    }

    @Test
    void payWithCardTokenDeclined() throws Exception {
        when(paymentRequestSender.sendPaymentRequest(any(CloudPaymentRequest.class))).thenReturn(testData.transactionDeclinedResponse());

        mockMvc.perform(post("/app/order/payment")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.order4_PaymentDto())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.PAYMENT_TRANSACTION_DECLINED.toString()))
                .andExpect(jsonPath("$.userMessage").value(testData.transactionDeclinedResponse().getModel().getCardHolderMessage()));

        assertPaymentDeclinedSaved();
    }

    @Test
    void payWithCardTokenRequestFailed() throws Exception {
        when(paymentRequestSender.sendPaymentRequest(any(CloudPaymentRequest.class))).thenReturn(null);

        mockMvc.perform(post("/app/order/payment")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.order4_PaymentDto())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.REQUEST_SENDING_FAILED.toString()));

        assertPaymentFailedSaved();
    }

    @Test
    void payWithCardTokenBonusCollision() throws Exception {
        // забираем у тестового юзера бонусы
        User user = userRepository.findById(testData.user1().getId()).orElseThrow();
        user.getBonusAccount().setBonusAmount(0);
        userRepository.save(user);

        when(paymentRequestSender.sendPaymentRequest(any(CloudPaymentRequest.class))).thenReturn(testData.transactionApprovedResponse());

        mockMvc.perform(post("/app/order/payment")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.order4_PaymentDto())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.PAYMENT_TRANSACTION_DECLINED.toString()));

        assertBonusCollisionSaved();
        verify(pushEventListeners, times(1)).sendBonusCollisionPush(any(BonusCollisionEvent.class));
    }

    private void assertBonusCollisionSaved() {
        Order order = orderRepository.findById(testData.order4_PaymentDto().getOrderId()).orElseThrow();
        assertEquals(BONUS_COLLISION, order.getStatus());
        assertTrue(order.getCloudPayment().isPaymentCompleteRequired());
        assertFalse(order.isCheckStatus());
        assertFalse(order.isInProcessing());
    }

    private void assertOrderPaidSavedCorrectly() {
        Order order = orderRepository.findById(testData.order4_PaymentDto().getOrderId()).orElseThrow();
        assertEquals(PAID, order.getStatus());
        assertNotNull(order.getPaidTime());
        assertTrue(order.isInProcessing());
        assertTrue(order.isCheckStatus());
        CloudPayment cloudPayment = order.getCloudPayment();
        CloudPaymentResponse testModel = testData.transactionApprovedResponse().getModel();
        assertEquals(testModel.getTransactionId(), cloudPayment.getTransactionId());
        assertEquals(testModel.getStatus(), cloudPayment.getCloudPaymentStatus());
        assertEquals(testModel.getReason(), cloudPayment.getDeclineReason());
        assertEquals(testModel.getReasonCode(), cloudPayment.getDeclineReasonCode());
    }

    private void assertPaymentFailedSaved() {
        Order order = orderRepository.findById(testData.order4_PaymentDto().getOrderId()).orElseThrow();
        assertNull(order.getCloudPayment().getCloudPaymentStatus());
        assertNull(order.getCloudPayment().getDeclineReason());
        assertNull(order.getCloudPayment().getDeclineReasonCode());
    }

    private void assertPaymentDeclinedSaved() {
        Order order = orderRepository.findById(testData.order4_PaymentDto().getOrderId()).orElseThrow();
        assertEquals(NOT_PAID, order.getStatus());
        CloudPayment cloudPayment = order.getCloudPayment();
        CloudPaymentResponse testModel = testData.transactionDeclinedResponse().getModel();
        assertEquals(testModel.getTransactionId(), cloudPayment.getTransactionId());
        assertEquals(testModel.getStatus(), cloudPayment.getCloudPaymentStatus());
        assertEquals(testModel.getReason(), cloudPayment.getDeclineReason());
        assertEquals(testModel.getReasonCode(), cloudPayment.getDeclineReasonCode());
        assertFalse(order.isCheckStatus());
        assertFalse(order.isInProcessing());
    }

    @Test
    @Disabled
    void payment3DSCallBack() throws Exception {
        when(paymentRequestSender.sendPaResRequest(any(CloudPaymentRequest.class))).thenReturn(testData.transactionApprovedResponse());

        mockMvc.perform(post("/app/public/cloud/payment/threeDsCallback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("PaRes", "PaResValue")
                .param("MD", "1111"))
                .andDo(print())
                .andExpect(status().isOk());

        Order order = orderRepository.findByCloudPaymentTransactionIdFetchUser(1111L).orElseThrow();
        assertEquals(order.getStatus(), PAID);
        assertEquals(order.getCloudPayment().getCloudPaymentStatus(), CloudPaymentStatus.Authorized);
    }
}
package com.foodtech.back.integration.controller;

import com.foodtech.back.dto.payment.cloud.CloudPaymentRequest;
import com.foodtech.back.dto.payment.cloud.CloudPaymentResponse;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.entity.payment.cloud.BankCardBindRequest;
import com.foodtech.back.repository.payment.BankCardBindRequestRepository;
import com.foodtech.back.repository.payment.BankCardRepository;
import com.foodtech.back.service.event.card.CardBindDeclinedEvent;
import com.foodtech.back.service.event.card.CardBindFailedEvent;
import com.foodtech.back.service.event.card.CardBindSuccessEvent;
import com.foodtech.back.service.http.CloudPaymentRequestSender;
import com.foodtech.back.service.notification.ampq.RabbitMqEventListeners;
import com.foodtech.back.service.notification.ampq.RabbitMqQueueType;
import com.foodtech.back.service.notification.ampq.RabbitMqService;
import com.foodtech.back.util.ResponseCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static com.foodtech.back.IntegrationTestData.BIND_CARD_TRANSACTION_ID_USER_1;
import static com.foodtech.back.IntegrationTestData.USER_1_CARDS_AMOUNT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"classpath:delete-payment-test-data.sql", "classpath:delete-product-test-data.sql",
        "classpath:user-data.sql", "classpath:product-test-data.sql", "classpath:payment-test-data.sql"})
@Sql(scripts = {"classpath:delete-payment-test-data.sql", "classpath:delete-product-test-data.sql"}, executionPhase = AFTER_TEST_METHOD)
class CloudPaymentCardBindingControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Autowired
    private BankCardBindRequestRepository cardBindRequestRepository;

    @Autowired
    private BankCardRepository bankCardRepository;

    @MockBean
    public CloudPaymentRequestSender paymentRequestSender;

    @MockBean
    public RabbitMqService rabbitMqService;

    @MockBean
    public RabbitMqEventListeners rabbitMqEventListeners;

    @Test
    void bindCardWith3DS() throws Exception {

        when(paymentRequestSender.sendCardBindRequest(any(CloudPaymentRequest.class))).thenReturn(testData.need3DSResponse());
        when(rabbitMqService.createQueue(any(RabbitMqQueueType.class), anyString())).thenReturn(testData.need3DSTransactionResult().getQueueName());

        mockMvc.perform(post("/app/cloud/payment/bind")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.bindCardDto())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ResponseCode.OK.toString()))
                .andExpect(jsonPath("$.body.needThreeDs").value(true))
                .andExpect(jsonPath("$.body.acsUrl").value(testData.need3DSTransactionResult().getAcsUrl()))
                .andExpect(jsonPath("$.body.PaReq").value(testData.need3DSTransactionResult().getPaReq()))
                .andExpect(jsonPath("$.body.MD").value(testData.need3DSTransactionResult().getMerchantData()))
                .andExpect(jsonPath("$.body.TermUrl").value(testData.need3DSTransactionResult().getTermUrl()))
                .andExpect(jsonPath("$.body.amqpHost").value(testData.need3DSTransactionResult().getAmqpHost()))
                .andExpect(jsonPath("$.body.queueName").value(testData.need3DSTransactionResult().getQueueName()))
                .andReturn();

        assertBindCardRequestSaved();
    }

    private void assertBindCardRequestSaved() {
        BankCardBindRequest bindRequest = cardBindRequestRepository
                .findByTransactionIdEquals(testData.need3DSResponse().getModel().getTransactionId()).orElseThrow();
        assertEquals(bindRequest.getUser().getId(), testData.user1().getId());
    }

    @Test
    void bindCardWithout3DS() throws Exception {
        when(paymentRequestSender.sendCardBindRequest(any(CloudPaymentRequest.class))).thenReturn(testData.transactionApprovedResponse());
        when(rabbitMqService.createQueue(any(RabbitMqQueueType.class), anyString())).thenReturn(testData.need3DSTransactionResult().getQueueName());

        CloudPaymentResponse model = testData.transactionApprovedResponse().getModel();
        String expectedCardMask = model.getCardFirstSix() + "******" + model.getCardLastFour();

        mockMvc.perform(post("/app/cloud/payment/bind")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.bindCardDto())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ResponseCode.OK.toString()))
                .andExpect(jsonPath("$.body.needThreeDs").value(false))
                .andExpect(jsonPath("$.body.bankCard.cardMask").value(expectedCardMask))
                .andExpect(jsonPath("$.body.bankCard.cardType").value(model.getCardType()))
                .andExpect(jsonPath("$.body.bankCard.actual").value(true));

        assertCardWithout3DSSaved();
        assertOnlyOneCardActual();
    }

    private void assertCardWithout3DSSaved() {
        BankCard card = bankCardRepository.findFirstByUserIdAndActualTrue(testData.user1().getId()).orElseThrow();
        assertTrue(card.isActual());
        CloudPaymentResponse testModel = testData.transactionApprovedResponse().getModel();
        assertTrue(card.getCardMask().startsWith(testModel.getCardFirstSix()));
        assertTrue(card.getCardMask().endsWith(testModel.getCardLastFour()));
        assertEquals(testModel.getCardType(), card.getCardType());
        assertEquals(testModel.getIssuer(), card.getCardIssuer());
    }

    private void assertOnlyOneCardActual() {
        List<BankCard> cards = bankCardRepository.findByUserId(testData.user1().getId());
        assertEquals(USER_1_CARDS_AMOUNT + 1, cards.size());

        long actualNum = cards.stream().filter(BankCard::isActual).count();
        assertEquals(1, actualNum);
    }

    @Test
    void bindCardWith3DSRequestFailed() throws Exception {
        when(paymentRequestSender.sendCardBindRequest(any(CloudPaymentRequest.class))).thenReturn(null);

        mockMvc.perform(post("/app/cloud/payment/bind")
                .header("Authorization", testData.user2AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.bindCardDto())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.REQUEST_SENDING_FAILED.toString()));

        assertBindRequestNotSaved();
    }

    private void assertBindRequestNotSaved() {
        Optional<BankCardBindRequest> user2BindCardRequest = cardBindRequestRepository.findByUserId(testData.user2().getId());
        assertTrue(user2BindCardRequest.isEmpty());
    }

    @Test
    void bindCardWithout3DSDecline() throws Exception {
        when(paymentRequestSender.sendCardBindRequest(any(CloudPaymentRequest.class))).thenReturn(testData.transactionDeclinedResponse());

        mockMvc.perform(post("/app/cloud/payment/bind")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.bindCardDto())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.CARD_BINDING_FAILED.toString()))
                .andExpect(jsonPath("$.userMessage").value(testData.transactionDeclinedResponse().getModel().getCardHolderMessage()));

        assertCardNotSavedAndOnlyOneActual();
    }

    @Test
    void bindCard3DSCallbackSuccess() throws Exception {
        when(paymentRequestSender.sendPaResRequest(any(CloudPaymentRequest.class))).thenReturn(testData.transactionApprovedResponse());

        mockMvc.perform(post("/app/public/cloud/bind/threeDsCallback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("PaRes", "PaResValue")
                .param("MD", String.valueOf(BIND_CARD_TRANSACTION_ID_USER_1)))
                .andDo(print())
                .andExpect(status().isOk());

        assertBindRequestSuccessResultSaved();
        assertNewCardSaved();
        assertOnlyOneCardActual();
        verify(rabbitMqEventListeners, times(1)).sendCardBindSuccessMsg(any(CardBindSuccessEvent.class));
    }

    private void assertBindRequestSuccessResultSaved() {
        BankCardBindRequest bindRequest = cardBindRequestRepository.findByTransactionIdEquals(BIND_CARD_TRANSACTION_ID_USER_1).orElseThrow();
        assertEquals(ResponseCode.OK.toString(), bindRequest.getResult());
    }

    private void assertNewCardSaved() {
        BankCard card = bankCardRepository.findFirstByUserIdAndActualTrue(testData.user1().getId()).orElseThrow();
        assertTrue(card.isActual());
        CloudPaymentResponse testBindingResultData = testData.transactionApprovedResponse().getModel();
        assertTrue(card.getCardMask().startsWith(testBindingResultData.getCardFirstSix()));
        assertTrue(card.getCardMask().endsWith(testBindingResultData.getCardLastFour()));
        assertEquals(testBindingResultData.getCardType(), card.getCardType());
        assertEquals(testBindingResultData.getIssuer(), card.getCardIssuer());
    }

    @Test
    void bindCard3DSCallbackDecline() throws Exception {
        when(paymentRequestSender.sendPaResRequest(any(CloudPaymentRequest.class))).thenReturn(testData.transactionDeclinedResponse());

        mockMvc.perform(post("/app/public/cloud/bind/threeDsCallback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("PaRes", "PaResValue")
                .param("MD", String.valueOf(BIND_CARD_TRANSACTION_ID_USER_1)))
                .andDo(print())
                .andExpect(status().isOk());

        assertBindRequestDeclineResultSaved();
        assertCardNotSavedAndOnlyOneActual();
        verify(rabbitMqEventListeners, times(1)).sendCardBindDeclinedMsg(any(CardBindDeclinedEvent.class));
    }

    private void assertBindRequestDeclineResultSaved() {
        BankCardBindRequest bindRequest = cardBindRequestRepository.findByTransactionIdEquals(BIND_CARD_TRANSACTION_ID_USER_1).orElseThrow();
        assertEquals(testData.transactionDeclinedResponse().getModel().getReason(), bindRequest.getResult());
    }

    @Test
    void bindCard3DSCallbackRequestFailed() throws Exception {
        when(paymentRequestSender.sendPaResRequest(any(CloudPaymentRequest.class))).thenReturn(null);

        mockMvc.perform(post("/app/public/cloud/bind/threeDsCallback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("PaRes", "PaResValue")
                .param("MD", String.valueOf(BIND_CARD_TRANSACTION_ID_USER_1)))
                .andDo(print())
                .andExpect(status().isOk());

        assertPaResRequestFailedResultSaved();
        assertCardNotSavedAndOnlyOneActual();
        verify(rabbitMqEventListeners, times(1)).sendCardBindFailedMsg(any(CardBindFailedEvent.class));
    }

    private void assertPaResRequestFailedResultSaved() {
        BankCardBindRequest bindRequest = cardBindRequestRepository.findByTransactionIdEquals(BIND_CARD_TRANSACTION_ID_USER_1).orElseThrow();
        assertEquals("Bad response", bindRequest.getResult());
    }

    private void assertCardNotSavedAndOnlyOneActual() {
        List<BankCard> user1Cards = bankCardRepository.findByUserId(testData.user1().getId());
        assertEquals(USER_1_CARDS_AMOUNT, user1Cards.size());

        long actualNum = user1Cards.stream().filter(BankCard::isActual).count();
        assertEquals(1, actualNum);
    }
}
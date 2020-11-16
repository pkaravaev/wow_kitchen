package com.foodtech.back.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.repository.payment.BankCardRepository;
import com.foodtech.back.util.ResponseCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"classpath:user-data.sql"})
class BankCardControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Autowired
    BankCardRepository cardRepository;

    @Test
    void getCards() throws Exception {

        MvcResult result = mockMvc.perform(get("/app/user/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<BankCard> bankCards = mapResult(result.getResponse().getContentAsString(), new TypeReference<>() {});

        assertEquals(2, bankCards.size());
        Set<String> cardMasks = bankCards.stream().map(BankCard::getCardMask).collect(Collectors.toSet());
        assertTrue(cardMasks.containsAll(Set.of(testData.bankCard1().getCardMask(), testData.bankCard2().getCardMask())));
    }

    @Test
    void setActualCard() throws Exception {

        checkSetActualCardTestData();

        mockMvc.perform(post("/app/user/card/actual/" + testData.bankCard2().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertActualCard(testData.bankCard2().getId(), testData.bankCard1().getId());
        assertOnlyOneActualCard();
    }

    @Test
    void setActualCardAlreadyActual() throws Exception {

        checkSetActualCardTestData();

        mockMvc.perform(post("/app/user/card/actual/" + testData.bankCard1().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertActualCard(testData.bankCard1().getId(), testData.bankCard2().getId());
        assertOnlyOneActualCard();
    }

    @Test
    void setActualNotFound() throws Exception {

        mockMvc.perform(post("/app/user/card/actual/" + 999)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.REQUEST_INVALID.toString()));

        assertActualCard(testData.bankCard1().getId(), testData.bankCard2().getId());
    }

    private void checkSetActualCardTestData() {
        BankCard card = cardRepository.findById(testData.bankCard2().getId()).orElseThrow();
        assertFalse(card.isActual());
    }

    private void assertActualCard(Long actualId, Long notActualId) {
        BankCard actualCard = cardRepository.findById(actualId).orElseThrow();
        assertTrue(actualCard.isActual());
        BankCard notActual = cardRepository.findById(notActualId).orElseThrow();
        assertFalse(notActual.isActual());
    }

    private void assertOnlyOneActualCard() {
        List<BankCard> cards = cardRepository.findByUserId(testData.user1().getId());
        long actualCardsNum = cards.stream().filter(BankCard::isActual).count();
        assertEquals(1, actualCardsNum);
    }

    private void assertActualCardNotChanged() {
        BankCard card1 = cardRepository.findById(testData.bankCard1().getId()).orElseThrow();
        assertTrue(card1.isActual());
        BankCard card2 = cardRepository.findById(testData.bankCard2().getId()).orElseThrow();
        assertFalse(card2.isActual());
    }
}

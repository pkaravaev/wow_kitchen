package com.foodtech.back.unit.service.model;

import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.repository.payment.BankCardRepository;
import com.foodtech.back.service.model.BankCardServiceImpl;
import com.foodtech.back.unit.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BankCardServiceImplUnitTest extends AbstractUnitTest {

    @Autowired
    BankCardServiceImpl cardService;

    @MockBean
    BankCardRepository cardRepository;

    @Test
    void addNew_CardOrder1() {
        User user = user();
        BankCard card1 = card1Actual();
        BankCard card2 = card2();
        BankCard card3 = card3();
        BankCard newCard = newCard();
        when(cardRepository.findByUserId(user.getId())).thenReturn(List.of(card1, card2, card3));

        cardService.add(newCard);

        verify(cardRepository, times(1)).save(newCard);
        assertActualSetCorrect(newCard, card1, card2, card3);
    }

    @Test
    void addNew_CardOrder2() {
        User user = user();
        BankCard card1 = card1Actual();
        BankCard card2 = card2();
        BankCard card3 = card3();
        BankCard newCard = newCard();
        when(cardRepository.findByUserId(user.getId())).thenReturn(List.of(card2, card1, card3));

        cardService.add(newCard);

        verify(cardRepository, times(1)).save(newCard);
        assertActualSetCorrect(newCard, card1, card2, card3);
    }

    @Test
    void addNew_CardOrder3() {
        User user = user();
        BankCard card1 = card1Actual();
        BankCard card2 = card2();
        BankCard card3 = card3();
        BankCard newCard = newCard();
        when(cardRepository.findByUserId(user.getId())).thenReturn(List.of(card2, card3, card1));

        cardService.add(newCard);

        verify(cardRepository, times(1)).save(newCard);
        assertActualSetCorrect(newCard, card1, card2, card3);
    }

    @Test
    void addNew_NoCardExisted() {
        User user = user();
        BankCard newCard = newCard();
        when(cardRepository.findByUserId(user.getId())).thenReturn(Collections.emptyList());

        cardService.add(newCard);

        verify(cardRepository, times(1)).save(newCard);
        assertTrue(newCard.isActual());
    }

    @Test
    void addNew_NoActualCardExisted() {
        User user = user();
        BankCard card2 = card2();
        BankCard card3 = card3();
        BankCard newCard = newCard();
        when(cardRepository.findByUserId(user.getId())).thenReturn(List.of(card2, card3));

        cardService.add(newCard);

        verify(cardRepository, times(1)).save(newCard);
        assertActualSetCorrect(newCard, card2, card3);
    }

    @Test
    void addExisted_Actual() {
        User user = user();
        BankCard card1 = card1Actual();
        BankCard card2 = card2();
        BankCard card3 = card3();
        when(cardRepository.findByUserId(user.getId())).thenReturn(List.of(card1, card2, card3));

        cardService.add(card1);

        verify(cardRepository, times(0)).save(any(BankCard.class));
        assertActualSetCorrect(card1, card2, card3);
    }

    @Test
    void addExisted_NotActual() {
        User user = user();
        BankCard card1 = card1Actual();
        BankCard card2 = card2();
        BankCard card3 = card3();
        when(cardRepository.findByUserId(user.getId())).thenReturn(List.of(card1, card2, card3));

        cardService.add(card2);

        verify(cardRepository, times(0)).save(any(BankCard.class));
        assertActualSetCorrect(card2, card1, card3);
    }

    @Test
    void setActual_CardOrder1() {
        User user = user();
        BankCard card1 = card1Actual();
        BankCard card2 = card2();
        BankCard card3 = card3();
        when(cardRepository.findByUserId(user.getId())).thenReturn(List.of(card1, card2, card3));

        cardService.setActual(user.getId(), card2.getId());

        assertActualSetCorrect(card2, card1, card3);
    }

    @Test
    void setActual_CardOrder2() {
        User user = user();
        BankCard card1 = card1Actual();
        BankCard card2 = card2();
        BankCard card3 = card3();
        when(cardRepository.findByUserId(user.getId())).thenReturn(List.of(card2, card1, card3));

        cardService.setActual(user.getId(), card2.getId());

        assertActualSetCorrect(card2, card1, card3);
    }

    @Test
    void setActual_CardOrder3() {
        User user = user();
        BankCard card1 = card1Actual();
        BankCard card2 = card2();
        BankCard card3 = card3();
        when(cardRepository.findByUserId(user.getId())).thenReturn(List.of(card2, card3, card1));

        cardService.setActual(user.getId(), card2.getId());

        assertActualSetCorrect(card2, card1, card3);
    }

    @Test
    void setActual_AlreadyActual() {
        User user = user();
        BankCard card1 = card1Actual();
        BankCard card2 = card2();
        BankCard card3 = card3();
        when(cardRepository.findByUserId(user.getId())).thenReturn(List.of(card1, card2, card3));

        cardService.setActual(user.getId(), card1.getId());

        assertActualSetCorrect(card1, card2, card3);
    }

    @Test
    void setActual_NotFound() {
        User user = user();
        BankCard card1 = card1Actual();
        BankCard card2 = card2();
        BankCard card3 = card3();
        when(cardRepository.findByUserId(user.getId())).thenReturn(List.of(card2, card1, card3));

        assertThrows(NoSuchElementException.class, () -> cardService.setActual(user.getId(), 999L));
    }

    private void assertActualSetCorrect(BankCard expectedActual, BankCard... expectedNotActual) {
        assertTrue(expectedActual.isActual());
        Arrays.stream(expectedNotActual).forEach(c -> assertFalse(c.isActual()));
    }

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setCountryCode("7");
        user.setMobileNumber("7777777777");
        return user;
    }

    private static BankCard card1Actual() {
        BankCard card1 = new BankCard();
        card1.setId(1L);
        card1.setActual(true);
        card1.setUser(user());
        card1.setCardIssuer("Iron Bank Of Braavos");
        card1.setCardMask("666666******4444");
        card1.setCardType("VISA");
        card1.setToken("CARD_1_TOKEN");
        return card1;
    }

    private static BankCard card2() {
        BankCard card2 = new BankCard();
        card2.setId(2L);
        card2.setActual(false);
        card2.setUser(user());
        card2.setCardIssuer("Iron Bank Of Braavos");
        card2.setCardMask("777777******5555");
        card2.setCardType("VISA");
        card2.setToken("CARD_2_TOKEN");
        return card2;
    }

    private static BankCard card3() {
        BankCard card3 = new BankCard();
        card3.setId(3L);
        card3.setActual(false);
        card3.setUser(user());
        card3.setCardIssuer("Iron Bank Of Braavos");
        card3.setCardMask("888888******6666");
        card3.setCardType("MasterCard");
        card3.setToken("CARD_3_TOKEN");
        return card3;
    }

    private static BankCard newCard() {
        BankCard newCard = new BankCard();
        newCard.setActual(true);
        newCard.setUser(user());
        newCard.setCardIssuer("Iron Bank Of Braavos");
        newCard.setCardMask("999999******7777");
        newCard.setCardType("MasterCard");
        newCard.setToken("NEW_CARD_TOKEN");
        return newCard;
    }

}
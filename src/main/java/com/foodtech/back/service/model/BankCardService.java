package com.foodtech.back.service.model;

import com.foodtech.back.entity.payment.BankCard;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BankCardService {
    List<BankCard> getCards(Long userId);

    Optional<BankCard> getActualCard(Long userId);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    BankCard add(BankCard newCard);

    @Transactional
    BankCard setActual(Long userId, Long cardId);

}

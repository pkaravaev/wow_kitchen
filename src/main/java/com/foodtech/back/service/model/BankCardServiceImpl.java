package com.foodtech.back.service.model;

import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.repository.payment.BankCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BankCardServiceImpl implements BankCardService {

    private BankCardRepository bankCardRepository;

    public BankCardServiceImpl(BankCardRepository bankCardRepository) {
        this.bankCardRepository = bankCardRepository;
    }

    @Override
    public List<BankCard> getCards(Long userId) {
        return bankCardRepository.findByUserId(userId);
    }

    @Override
    public Optional<BankCard> getActualCard(Long userId) {
        return bankCardRepository.findFirstByUserIdAndActualTrue(userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BankCard add(BankCard newCard) {
        List<BankCard> cards = getCards(newCard.getUser().getId());
        cards.forEach(c -> c.setActual(false));
        Optional<BankCard> equalExistingCard = findEqual(newCard, cards);
        return equalExistingCard
                .map(this::updateExisting)
                .orElseGet(() -> bankCardRepository.save(newCard));
    }

    private BankCard updateExisting(BankCard card) {
        card.setActual(true);
        return card;
    }

    private Optional<BankCard> findEqual(BankCard newCard, List<BankCard> cards) {
        return cards
                .stream()
                .filter(c -> c.getCardMask().equals(newCard.getCardMask()))
                .findFirst();
    }

    @Override
    @Transactional
    public BankCard setActual(Long userId, Long cardId) {
        List<BankCard> cards = getCards(userId);
        cards.forEach(c -> c.setActual(c.getId().equals(cardId)));
        return actual(cards);
    }

    private BankCard actual(List<BankCard> cards) {
        return cards
                .stream()
                .filter(BankCard::isActual)
                .findFirst()
                .orElseThrow();
    }
}

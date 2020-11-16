package com.foodtech.back.repository.payment;

import com.foodtech.back.entity.payment.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankCardRepository extends JpaRepository<BankCard, Long> {

    List<BankCard> findByUserId(Long userId);

    Optional<BankCard> findFirstByUserIdAndActualTrue(Long userId);

}

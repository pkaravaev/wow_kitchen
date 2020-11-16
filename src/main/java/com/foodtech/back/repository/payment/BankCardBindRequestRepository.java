package com.foodtech.back.repository.payment;

import com.foodtech.back.entity.payment.cloud.BankCardBindRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BankCardBindRequestRepository extends JpaRepository<BankCardBindRequest, Long> {

    Optional<BankCardBindRequest> findByTransactionIdEquals(Long transactionId);

    Optional<BankCardBindRequest> findByUserId(Long userId);

    @Query("SELECT b.queueName FROM BankCardBindRequest b WHERE b.queueName IS NOT NULL AND b.created <?1")
    List<String> findQueueNamesForDelete(LocalDateTime date);

    @Modifying
    @Query("UPDATE BankCardBindRequest b SET b.queueName = NULL WHERE b.queueName IN ?1")
    void setNullToDeletedQueues(List<String> queueNames);
}

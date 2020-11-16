package com.foodtech.back.repository.model;

import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.OrderStatus;
import com.foodtech.back.entity.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /*
    Методы для админки
     */

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findAll(Pageable pageable);

    /*
    Методы для приложения
     */

    // Запрос для истории заказов юзера, не демонстрируем заказы, которые не были оплачены и отправлены в iiko
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items it " +
            "LEFT JOIN FETCH o.address " +
            "LEFT JOIN FETCH it.product p " +
            "LEFT JOIN FETCH o.bankCard " +
            "WHERE o.user =?1 AND o.status IN ?2 " +
            "ORDER BY o.id DESC")
    List<Order> findAllByUserFetchProductsAndAddress(User user, List<OrderStatus> orderStatuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Order> findByIdAndUserIdAndStatusIn(Long orderId, Long userId, Set<OrderStatus> statuses);

    Optional<Order> findFirstByUserIdAndInProcessingIsTrueOrderByCreatedDesc(Long userId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items it " +
            "LEFT JOIN FETCH o.address " +
            "LEFT JOIN FETCH it.product " +
            "LEFT JOIN FETCH o.bankCard " +
            "WHERE o.id =?1 AND o.user.id =?2")
    Optional<Order> findByIdAndUserIdFetchProductsAndAddress(Long orderId, Long userId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user " +
            "WHERE o.cloudPayment.transactionId =?1")
    Optional<Order> findByCloudPaymentTransactionIdFetchUser(Long transactionId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items it " +
            "LEFT JOIN FETCH o.address " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH it.product p " +
            "WHERE o.id =?1")
    Optional<Order> findByIdFetchUserAndProductsAndAddress(Long orderId);

    @Query("SELECT o.status FROM Order o " +
            "WHERE o.id =?1 AND o.user.id =?2")
    Optional<OrderStatus> findStatusByIdAndUserId(Long orderId, Long userId);

    List<Order> findByCheckStatusTrue();

    @Query("SELECT o.statusQueueName FROM Order o WHERE o.statusQueueName IS NOT NULL AND o.created <?1")
    List<String> findQueueNamesForDelete(LocalDateTime date);

    @Query("SELECT o.id FROM Order o " +
            "WHERE o.cloudPayment.paymentCompleteRequired = true AND o.status IN ?1")
    List<Long> findPaymentCompleteRequiredIds(Set<OrderStatus> statuses);

    @Modifying
    @Query("UPDATE Order o SET o.statusQueueName = NULL WHERE o.statusQueueName IN ?1")
    void setNullToDeletedQueues(List<String> queueNames);
}

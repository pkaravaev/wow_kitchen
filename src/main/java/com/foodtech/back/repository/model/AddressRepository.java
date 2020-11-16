package com.foodtech.back.repository.model;

import com.foodtech.back.entity.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByCreatedDesc(Long userId);

    Optional<Address> findFirstByUserIdAndActualTrue(Long userId);

    @Query("SELECT a FROM Address a " +
            "LEFT JOIN FETCH a.deliveryTerminal t " +
            "LEFT JOIN FETCH t.openingHours " +
            "WHERE a.actual = true " +
            "AND a.user.id =?1")
    Optional<Address> findByUserIdAndActualTrueWithDeliveryTerminalAndOpeningHours(Long userId);

    Optional<Address> findFirstByUserIdOrderByCreatedDesc(Long userId);

}

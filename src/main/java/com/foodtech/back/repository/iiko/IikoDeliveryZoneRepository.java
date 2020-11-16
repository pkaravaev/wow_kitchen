package com.foodtech.back.repository.iiko;

import com.foodtech.back.entity.model.KitchenDeliveryTerminal;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import com.foodtech.back.entity.model.iiko.DeliveryZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IikoDeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {

    @Override
    @Query("SELECT DISTINCT z FROM DeliveryZone z " +
            "LEFT JOIN FETCH z.coordinates c " +
            "ORDER BY c.locationOrder")
    List<DeliveryZone> findAll();

    @Query("SELECT DISTINCT z FROM DeliveryZone z " +
            "LEFT JOIN FETCH z.coordinates c " +
            "WHERE z.active = true " +
            "ORDER BY c.locationOrder")
    List<DeliveryZone> findAllActive();

    Optional<DeliveryZone> findByCoordinates(DeliveryCoordinate coordinate);

    boolean existsByDeliveryTerminalAndActiveIsTrue(KitchenDeliveryTerminal deliveryTerminal);
}

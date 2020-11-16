package com.foodtech.back.service.model;

import com.foodtech.back.entity.model.Address;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AddressService {

    List<Address> getAllByUser(Long id);

    @Transactional
    Address getActualByUser(Long userId);

    @Transactional
    Address add(User user, Address newAddress, DeliveryCoordinate coordinate);

    @Transactional
    Address setActual(Long userId, Long addressId);
}

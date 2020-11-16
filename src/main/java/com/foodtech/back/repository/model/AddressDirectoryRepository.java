package com.foodtech.back.repository.model;

import com.foodtech.back.entity.model.AddressDirectory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressDirectoryRepository extends JpaRepository<AddressDirectory, Long> {

    List<AddressDirectory> findByDeliveryZone_ActiveIsTrue();
}

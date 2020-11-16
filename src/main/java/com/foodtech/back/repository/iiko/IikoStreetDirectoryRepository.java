package com.foodtech.back.repository.iiko;

import com.foodtech.back.entity.model.iiko.IikoStreetDirectory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IikoStreetDirectoryRepository extends JpaRepository<IikoStreetDirectory, String> {

    boolean existsByNameEquals(String name);
}

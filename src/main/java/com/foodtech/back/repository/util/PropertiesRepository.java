package com.foodtech.back.repository.util;

import com.foodtech.back.entity.util.AppProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropertiesRepository extends JpaRepository<AppProperty, Integer> {

    List<AppProperty> findAllByOrderById();

    Optional<AppProperty> findByNameEquals(String name);

    List<AppProperty> findByNameIn(List<String> properties);
}

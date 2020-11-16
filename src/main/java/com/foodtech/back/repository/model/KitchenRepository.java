package com.foodtech.back.repository.model;

import com.foodtech.back.entity.model.Kitchen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitchenRepository extends JpaRepository<Kitchen, Long> {
}

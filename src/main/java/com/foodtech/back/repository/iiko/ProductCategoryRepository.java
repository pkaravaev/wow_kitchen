package com.foodtech.back.repository.iiko;

import com.foodtech.back.entity.model.iiko.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, String> {

    @Query("SELECT DISTINCT c FROM ProductCategory c " +
            "LEFT JOIN FETCH c.products p " +
            "WHERE p.inStopList = false " +
            "AND p.includedInMenu = true " +
            "ORDER BY c.viewOrder")
    List<ProductCategory> findCurrentMenu();
}

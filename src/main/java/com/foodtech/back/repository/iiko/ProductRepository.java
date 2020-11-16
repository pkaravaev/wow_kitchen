package com.foodtech.back.repository.iiko;

import com.foodtech.back.entity.model.iiko.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, String> {

    /**
     Запросы для админки
     */

    List<Product> findAllByOrderByIncludedInMenuDescProductCategoryNameAsc();

    List<Product> findByIdIn(List<String> idList);

    /**
     Запросы для приложения
     */
    List<Product> findByIdInAndInStopListFalseAndIncludedInMenuTrue(Set<String> idList);

    @Modifying
    @Query("UPDATE  Product p SET p.inStopList = TRUE WHERE p.id IN ?1")
    @Transactional
    void updateStopList(Set<String> idList);

    @Modifying
    @Query("UPDATE Product p SET p.includedInMenu = FALSE WHERE p.id NOT IN ?1")
    @Transactional
    void refreshMenu(Set<String> idList);

    @Modifying
    @Query("UPDATE Product p SET p.inStopList = FALSE WHERE p.id NOT IN ?1")
    @Transactional
    void refreshStopList(Set<String> idList);

    @Modifying
    @Query("UPDATE Product p SET p.inStopList = FALSE")
    @Transactional
    void refreshStopList();
}

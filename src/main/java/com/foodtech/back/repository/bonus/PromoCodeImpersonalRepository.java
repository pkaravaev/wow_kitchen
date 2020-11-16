package com.foodtech.back.repository.bonus;

import com.foodtech.back.entity.bonus.PromoCodeImpersonal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromoCodeImpersonalRepository extends JpaRepository<PromoCodeImpersonal, Long> {

    Optional<PromoCodeImpersonal> findByPromoCodeEquals(String code);

    boolean existsByPromoCodeEquals(String code);
}

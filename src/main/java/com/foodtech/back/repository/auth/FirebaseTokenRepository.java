package com.foodtech.back.repository.auth;

import com.foodtech.back.entity.auth.FirebaseToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FirebaseTokenRepository extends JpaRepository<FirebaseToken, Long> {

    Optional<FirebaseToken> findByUserId(Long userId);

    Optional<FirebaseToken> findByTokenEquals(String token);

    boolean existsByTokenEquals(String token);
}

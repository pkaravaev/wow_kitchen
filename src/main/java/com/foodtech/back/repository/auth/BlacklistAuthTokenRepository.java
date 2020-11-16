package com.foodtech.back.repository.auth;

import com.foodtech.back.entity.auth.BlacklistAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistAuthTokenRepository extends JpaRepository<BlacklistAuthToken, Long> {

    boolean existsByTokenEquals(String token);
}

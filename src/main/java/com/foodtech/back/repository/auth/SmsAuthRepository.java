package com.foodtech.back.repository.auth;

import com.foodtech.back.entity.auth.SmsAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmsAuthRepository extends JpaRepository<SmsAuth, Long> {

    Optional<SmsAuth> findByCountryCodeAndMobileNumber(String countryCode, String mobile);

    Optional<SmsAuth> findByCountryCodeAndMobileNumberAndUsed(String countryCode, String mobile, boolean used);
}

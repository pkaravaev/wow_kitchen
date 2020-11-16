package com.foodtech.back.unit.service.auth;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.auth.CredentialsAuthDto;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.entity.auth.SmsAuth;
import com.foodtech.back.repository.auth.SmsAuthRepository;
import com.foodtech.back.service.auth.SmsCodeCredentialsChecker;
import com.foodtech.back.util.ResponseCode;
import com.foodtech.back.util.exceptions.SmsCheckFailedException;
import com.foodtech.back.util.exceptions.SmsNotFoundException;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("unit")
@TestPropertySource(value = {
        "classpath:sms.properties",
        "classpath:crypt.properties",
        "classpath:iiko.properties",
        "classpath:delivery.properties",
        "classpath:push.properties",
        "classpath:payment.properties",
        "classpath:client_messages.properties",
        "classpath:rabbitmq.properties",

        "classpath:payment-dev.properties"
})
class SmsCodeCredentialsCheckerUnitTest {

    private static final String COUNTRY_CODE = "7";
    private static final String MOBILE_NUMBER = "9999999999";
    private static final FullMobileNumber FULL_MOBILE_NUMBER = new FullMobileNumber(COUNTRY_CODE, MOBILE_NUMBER);
    private static final String CODE = "0000";

    private static SmsAuth smsAuth() {
        SmsAuth smsAuth = new SmsAuth();
        smsAuth.setSmsCode(DigestUtils.md5Hex(CODE));
        smsAuth.setMobileNumber(MOBILE_NUMBER);
        smsAuth.setCountryCode(COUNTRY_CODE);
        smsAuth.setUsed(false);
        smsAuth.setAttempt(0);
        smsAuth.setLastSend(LocalDateTime.now());
        return smsAuth;
    }

    @Autowired
    SmsCodeCredentialsChecker credentialsChecker;

    @Autowired
    ResourcesProperties properties;

    @MockBean
    SmsAuthRepository smsAuthRepository;

    @Test
    void checkCodeSuccess() {
        //when
        SmsAuth smsAuth = smsAuth();
        when(smsAuthRepository.findByCountryCodeAndMobileNumberAndUsed(COUNTRY_CODE, MOBILE_NUMBER, false)).thenReturn(Optional.of(smsAuth));

        //do
        CredentialsAuthDto authDto = new CredentialsAuthDto();
        authDto.setFullNumber(FULL_MOBILE_NUMBER);
        authDto.setSmsCode(CODE);
        credentialsChecker.checkCredentials(authDto);

        assertTrue(smsAuth.getUsed());
    }

    @Test
    void checkCodeSmsAuthNotFound() {
        //when
        when(smsAuthRepository.findByCountryCodeAndMobileNumberAndUsed(COUNTRY_CODE, MOBILE_NUMBER, false)).thenReturn(Optional.empty());

        //do
        CredentialsAuthDto authDto = new CredentialsAuthDto();
        authDto.setFullNumber(FULL_MOBILE_NUMBER);
        authDto.setSmsCode(CODE);
        assertThrows(SmsNotFoundException.class, () -> credentialsChecker.checkCredentials(authDto));
    }

    @Test
    void checkCodeExpired() {
        //when
        SmsAuth smsAuth = smsAuth();
        smsAuth.setLastSend(LocalDateTime.of(2019, 1, 1, 0, 0));
        when(smsAuthRepository.findByCountryCodeAndMobileNumberAndUsed(COUNTRY_CODE, MOBILE_NUMBER, false)).thenReturn(Optional.of(smsAuth));

        //do
        CredentialsAuthDto authDto = new CredentialsAuthDto();
        authDto.setFullNumber(FULL_MOBILE_NUMBER);
        authDto.setSmsCode(CODE);
        try {
            credentialsChecker.checkCredentials(authDto);
        } catch (SmsCheckFailedException ex) {
            assertEquals(ex.getUserNumber(), smsAuth.getMobileNumber());
            assertEquals(ex.getReason(), ResponseCode.SMS_EXPIRED);
            assertEquals(ex.getUserMessage(), properties.getSmsCodeExpiredMsg());
        }
    }

    @Test
    void checkCodeInvalid() {
        //when
        when(smsAuthRepository.findByCountryCodeAndMobileNumberAndUsed(COUNTRY_CODE, MOBILE_NUMBER, false)).thenReturn(Optional.of(smsAuth()));

        //do
        CredentialsAuthDto authDto = new CredentialsAuthDto();
        authDto.setFullNumber(FULL_MOBILE_NUMBER);
        authDto.setSmsCode("INVALID_CODE");
        try {
            credentialsChecker.checkCredentials(authDto);
        } catch (SmsCheckFailedException ex) {
            assertEquals(smsAuth().getMobileNumber(), ex.getUserNumber());
            assertEquals(ResponseCode.SMS_INVALID, ex.getReason());
            assertEquals(properties.getSmsCodeInvalidMsg(), ex.getUserMessage());
        }
    }

    @Test
    void checkCodeMaxAttempts() {
        //when
        SmsAuth smsAuth = smsAuth();
        smsAuth.setAttempt(2);
        when(smsAuthRepository.findByCountryCodeAndMobileNumberAndUsed(COUNTRY_CODE, MOBILE_NUMBER, false)).thenReturn(Optional.of(smsAuth));

        //do
        CredentialsAuthDto authDto = new CredentialsAuthDto();
        authDto.setFullNumber(FULL_MOBILE_NUMBER);
        authDto.setSmsCode("INVALID_CODE");
        try {
            credentialsChecker.checkCredentials(authDto);
        } catch (SmsCheckFailedException ex) {
            assertEquals(smsAuth.getMobileNumber(), ex.getUserNumber());
            assertEquals(ResponseCode.SMS_MAX_ATTEMPT, ex.getReason());
            assertEquals(properties.getSmsCheckMaxAttemptMsg(), ex.getUserMessage());
        }
    }

}
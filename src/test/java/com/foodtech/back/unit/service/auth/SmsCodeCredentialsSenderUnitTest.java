package com.foodtech.back.unit.service.auth;

import com.foodtech.back.dto.auth.AuthDto;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.entity.auth.SmsAuth;
import com.foodtech.back.repository.auth.SmsAuthRepository;
import com.foodtech.back.service.auth.SmsCodeCredentialsSender;
import com.foodtech.back.service.notification.sms.SmsMessageDto;
import com.foodtech.back.service.notification.sms.SmsSender;
import com.foodtech.back.util.SmsUtil;
import com.foodtech.back.util.exceptions.SmsSendingFailedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
class SmsCodeCredentialsSenderUnitTest {

    private static final String COUNTRY_CODE = "7";
    private static final String MOBILE_NUMBER = "9999999999";
    private static final FullMobileNumber FULL_MOBILE_NUMBER = new FullMobileNumber(COUNTRY_CODE, MOBILE_NUMBER);
    private static final String CODE = "0000";
    private static final String MESSAGE = "Code: 0000";

    @MockBean
    SmsSender smsSender;

    @MockBean
    SmsAuthRepository smsAuthRepository;

    @MockBean
    SmsUtil smsUtil;

    @Autowired
    SmsCodeCredentialsSender sender;

    @Test
    void sendCredentialsNewAuth() {
        //when
        when(smsAuthRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE_NUMBER)).thenReturn(Optional.empty());
        when(smsUtil.generateAuthCode()).thenReturn(CODE);
        when(smsUtil.formAuthCodeSms(CODE)).thenReturn(MESSAGE);

        SmsMessageDto messageResult = new SmsMessageDto();
        messageResult.setSuccess(true);
        when(smsSender.send(any(SmsMessageDto.class))).thenReturn(messageResult);

        //do
        AuthDto authDto = new AuthDto();
        authDto.setFullNumber(FULL_MOBILE_NUMBER);
        sender.sendCredentials(authDto);

        //then
        verify(smsUtil, times(1)).generateAuthCode();
        verify(smsUtil, times(1)).formAuthCodeSms(CODE);

        SmsMessageDto messageDto = new SmsMessageDto();
        messageDto.setCountry(COUNTRY_CODE);
        messageDto.setMobNumber(MOBILE_NUMBER);
        messageDto.setContent(MESSAGE);
        verify(smsSender, times(1)).send(messageDto);

        SmsAuth smsAuth = new SmsAuth();
        smsAuth.setCountryCode(COUNTRY_CODE);
        smsAuth.setMobileNumber(MOBILE_NUMBER);
        verify(smsAuthRepository, times(1)).save(smsAuth);
    }

    @Test
    void sendCredentialsExistingSmsAuth() {
        //when
        SmsAuth existedSmsAuth = new SmsAuth();
        existedSmsAuth.setLastSend(LocalDateTime.now().minusMinutes(1L));
        when(smsAuthRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE_NUMBER)).thenReturn(Optional.of(existedSmsAuth));
        when(smsUtil.generateAuthCode()).thenReturn(CODE);
        when(smsUtil.formAuthCodeSms(CODE)).thenReturn(MESSAGE);

        SmsMessageDto messageResult = new SmsMessageDto();
        messageResult.setSuccess(true);
        when(smsSender.send(any(SmsMessageDto.class))).thenReturn(messageResult);

        //do
        AuthDto authDto = new AuthDto();
        authDto.setFullNumber(FULL_MOBILE_NUMBER);
        sender.sendCredentials(authDto);

        //then
        verify(smsUtil, times(1)).generateAuthCode();
        verify(smsUtil, times(1)).formAuthCodeSms(CODE);

        SmsMessageDto messageDto = new SmsMessageDto();
        messageDto.setCountry(COUNTRY_CODE);
        messageDto.setMobNumber(MOBILE_NUMBER);
        messageDto.setContent(MESSAGE);
        verify(smsSender, times(1)).send(messageDto);

        SmsAuth smsAuth = new SmsAuth();
        smsAuth.setCountryCode(COUNTRY_CODE);
        smsAuth.setMobileNumber(MOBILE_NUMBER);
        verify(smsAuthRepository, times(1)).save(smsAuth);
    }

    @Test
    void sendCredentialsSmsSendingFailed() {
        //when
        when(smsAuthRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE_NUMBER)).thenReturn(Optional.empty());
        when(smsUtil.generateAuthCode()).thenReturn(CODE);
        when(smsUtil.formAuthCodeSms(CODE)).thenReturn(MESSAGE);

        SmsMessageDto messageResult = new SmsMessageDto();
        messageResult.setSuccess(false);
        when(smsSender.send(any(SmsMessageDto.class))).thenReturn(messageResult);

        //do
        AuthDto authDto = new AuthDto();
        authDto.setFullNumber(FULL_MOBILE_NUMBER);
        assertThrows(SmsSendingFailedException.class, () -> sender.sendCredentials(authDto));

        SmsAuth smsAuth = new SmsAuth();
        smsAuth.setCountryCode(COUNTRY_CODE);
        smsAuth.setMobileNumber(MOBILE_NUMBER);
        verify(smsAuthRepository, times(0)).save(smsAuth);
    }
}
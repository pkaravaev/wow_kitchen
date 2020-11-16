package com.foodtech.back.service.auth;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.auth.CredentialsAuthDto;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.entity.auth.SmsAuth;
import com.foodtech.back.repository.auth.SmsAuthRepository;
import com.foodtech.back.util.exceptions.SmsCheckFailedException;
import com.foodtech.back.util.exceptions.SmsNotFoundException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.foodtech.back.util.ResponseCode.*;

@Service
public class SmsCodeCredentialsChecker implements AuthCredentialsChecker {

    public static final Integer MAX_SMS_ATTEMPTS = 3;
    public static final Long CODE_EXPIRATION_IN_HOUR = 1L;

    private final SmsAuthRepository smsAuthRepository;

    private final ResourcesProperties properties;

    public SmsCodeCredentialsChecker(SmsAuthRepository smsAuthRepository, ResourcesProperties properties) {
        this.smsAuthRepository = smsAuthRepository;
        this.properties = properties;
    }

    @Override
    @Transactional(noRollbackFor = SmsCheckFailedException.class)
    public void checkCredentials(CredentialsAuthDto authDto) {
        FullMobileNumber fullNumber = authDto.getFullNumber();
        String code = authDto.getSmsCode();

        if (isAppleReviewEmployee(code, fullNumber)) {
            return;
        }

        SmsAuth smsAuth = getSmsAuthNotUsed(fullNumber);
        checkSmsCodeNotExpired(smsAuth);
        checkSmsCodeIsValid(smsAuth, code);
        smsAuth.setUsed(true);
    }

    private void checkSmsCodeNotExpired(SmsAuth smsAuth) {
        if (smsAuth.getLastSend().plusHours(CODE_EXPIRATION_IN_HOUR).isBefore(LocalDateTime.now())) {
            smsAuth.setUsed(true);
            throw new SmsCheckFailedException(smsAuth.getMobileNumber(), SMS_EXPIRED, properties.getSmsCodeExpiredMsg());
        }
    }

    private void checkSmsCodeIsValid(SmsAuth smsAuth, String code) {
        if (!smsAuth.getSmsCode().equals(DigestUtils.md5Hex(code))) {
            smsAuth.setAttempt(smsAuth.getAttempt() + 1);
            checkSmsCodeMaxAttempts(smsAuth);
            throw new SmsCheckFailedException(smsAuth.getMobileNumber(), SMS_INVALID, properties.getSmsCodeInvalidMsg());
        }
    }

    private void checkSmsCodeMaxAttempts(SmsAuth smsAuth) {
        if (smsAuth.getAttempt() >= MAX_SMS_ATTEMPTS) {
            smsAuth.setUsed(true);
            throw new SmsCheckFailedException(smsAuth.getMobileNumber(), SMS_MAX_ATTEMPT, properties.getSmsCheckMaxAttemptMsg());
        }
    }

    private SmsAuth getSmsAuthNotUsed(FullMobileNumber fullNumber) {
        return smsAuthRepository.findByCountryCodeAndMobileNumberAndUsed(fullNumber.getCountryCode(),
                fullNumber.getMobileNumber(), false)
                .orElseThrow(() -> {throw new SmsNotFoundException(fullNumber.toString());});
    }

    /* Вход для проверяющих из эппл во время публикации приложения в эппл сторе */
    private boolean isAppleReviewEmployee(String code, FullMobileNumber number) {
        return "6969".equals(code) && number.equals(new FullMobileNumber("7", "9153121664"));
    }
}

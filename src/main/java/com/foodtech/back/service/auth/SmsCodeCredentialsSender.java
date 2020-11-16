package com.foodtech.back.service.auth;

import com.foodtech.back.dto.auth.AuthDto;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.entity.auth.SmsAuth;
import com.foodtech.back.repository.auth.SmsAuthRepository;
import com.foodtech.back.service.notification.sms.SmsMessageDto;
import com.foodtech.back.service.notification.sms.SmsSender;
import com.foodtech.back.util.SmsUtil;
import com.foodtech.back.util.exceptions.SmsSendingFailedException;
import com.foodtech.back.util.exceptions.SmsSendingNotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class SmsCodeCredentialsSender implements AuthCredentialsSender {

    private static final Long SMS_DELAY_TIME_MIN = 0L;

    private final SmsSender smsSender;

    private final SmsAuthRepository smsAuthRepository;

    private final SmsUtil smsUtil;

    public SmsCodeCredentialsSender(SmsSender smsSender, SmsAuthRepository smsAuthRepository, SmsUtil smsUtil) {
        this.smsSender = smsSender;
        this.smsAuthRepository = smsAuthRepository;
        this.smsUtil = smsUtil;
    }

    @Override
    public void sendCredentials(AuthDto authDto) {

        FullMobileNumber fullNumber = authDto.getFullNumber();
        log.info("Sms send request for '{}'", fullNumber);
        checkSendingAllowed(fullNumber);
        String code = smsUtil.generateAuthCode();
        SmsMessageDto sms = new SmsMessageDto();
        sms.setCountry(fullNumber.getCountryCode());
        sms.setMobNumber(fullNumber.getMobileNumber());
        sms.setContent(smsUtil.formAuthCodeSms(code));
        SmsMessageDto messageDto = smsSender.send(sms);
        checkSendingResult(messageDto, fullNumber);
        saveSmsAuth(fullNumber, code);
    }

    private void checkSendingAllowed(FullMobileNumber fullNumber) {
        Optional<SmsAuth> smsAuthOpt = getSmsAuth(fullNumber);
        if (smsAuthOpt.isEmpty()) {
            return;
        }

        boolean delayTimeExpired = smsAuthOpt.get().getLastSend().plusMinutes(SMS_DELAY_TIME_MIN).isBefore(LocalDateTime.now());
        if (!delayTimeExpired) {
            throw new SmsSendingNotAllowedException(fullNumber.toString());
        }
    }

    private void checkSendingResult(SmsMessageDto messageDto, FullMobileNumber fullNumber) {
        if (!messageDto.getSuccess()) {
            throw new SmsSendingFailedException(fullNumber.toString());
        }
    }

    private void saveSmsAuth(FullMobileNumber fullNumber, String code) {
        SmsAuth smsAuth = new SmsAuth();
        Optional<SmsAuth> smsAuthOpt = getSmsAuth(fullNumber);
        smsAuthOpt.ifPresent(prevValue -> smsAuth.setId(prevValue.getId()));

        smsAuth.setCountryCode(fullNumber.getCountryCode());
        smsAuth.setMobileNumber(fullNumber.getMobileNumber());
        smsAuth.setLastSend(LocalDateTime.now());
        smsAuth.setSmsCode(DigestUtils.md5Hex(code));
        smsAuth.setAttempt(0);
        smsAuth.setUsed(false);

        smsAuthRepository.save(smsAuth);
    }


    private Optional<SmsAuth> getSmsAuth(FullMobileNumber fullNumber) {
        return smsAuthRepository.findByCountryCodeAndMobileNumber(fullNumber.getCountryCode(),
                fullNumber.getMobileNumber());
    }
}

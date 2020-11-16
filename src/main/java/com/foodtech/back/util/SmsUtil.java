package com.foodtech.back.util;

import com.foodtech.back.config.ResourcesProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class SmsUtil {

    private static final String CODE_FORMAT = "%04d"; /* Формат кода 4 цифры - если в сгенерированном коде не хватает цифр, то подставляем 0 в начало */

    private final ResourcesProperties properties;

    public SmsUtil(ResourcesProperties properties) {
        this.properties = properties;
    }

    public String formAuthCodeSms(String code) {
        return properties.getSmsCodeDescription() + " " + code + "\n" + properties.getSmsFooter();
    }

    public String generateAuthCode() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return String.format(CODE_FORMAT,random.nextInt(10_000));
    }

}

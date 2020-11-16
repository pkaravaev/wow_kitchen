package com.foodtech.back.util;

import com.foodtech.back.dto.model.FullMobileNumber;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class ValidationUtil {

    public static void formMobileNumber(FullMobileNumber fullMobileNumber) {

        // Предусматриваем случай указания внутреннего кода 8 вместо международного 7 для России и Казахстана
        if ("8".equals(fullMobileNumber.getCountryCode())) {
            fullMobileNumber.setCountryCode("7");
        }

        String mobileNumber = fullMobileNumber.getMobileNumber();
        // Убираем ошибочной указанный код страны в мобильном номере (для России и Казахстана)
        if ("7".equals(fullMobileNumber.getCountryCode())
            && mobileNumber.length() > 10
            && (mobileNumber.startsWith("8") || mobileNumber.startsWith("7"))) {
            fullMobileNumber.setMobileNumber(mobileNumber.substring(1));
        }
    }

    // Минимальная длина тел. номеров в мире 5, максимальная 15
    public static boolean mobileNumberIsValid(FullMobileNumber fullNumber) {
        String mobileNumber = fullNumber.getMobileNumber();
        return mobileNumber.length() > 4 && mobileNumber.length() < 16;
    }

    public static Set<String> formPreferencesDontLikeSet(Set<String> dontLikeSet) {
        if (dontLikeSet.isEmpty()) {
            return dontLikeSet;
        }
        dontLikeSet.forEach(s -> s = s.replaceAll("#", "").trim().toLowerCase());
        return dontLikeSet;
    }
}

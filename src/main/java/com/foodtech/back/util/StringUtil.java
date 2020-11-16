package com.foodtech.back.util;

import com.foodtech.back.dto.model.FullMobileNumber;

import static java.util.Objects.requireNonNull;

public class StringUtil {

    public static String removeNonDigits(String str) {
        requireNonNull(str);

        return str.replaceAll("[^\\d]", "");
    }

    private static final String SPLITTER = "-";

    public static String formFullMobileNumberStr(String countryCode, String mobileNumber) {
        requireNonNull(countryCode);
        requireNonNull(mobileNumber);

        return countryCode + SPLITTER + mobileNumber;
    }

    public static String formFullMobileNumberStr(FullMobileNumber fullMobileNumber) {
        requireNonNull(fullMobileNumber);

        return fullMobileNumber.getCountryCode() + SPLITTER + fullMobileNumber.getMobileNumber();
    }

    public static FullMobileNumber splitFullMobileNumber(String fullMobileNumber) {
        requireNonNull(fullMobileNumber);

        String[] split = fullMobileNumber.split(SPLITTER);
        if (split.length < 2) {
            throw new IllegalArgumentException("Full mobile number is invalid");
        }

        String countryCode = split[0];
        String mobileNumber = split[1];
        return new FullMobileNumber(countryCode, mobileNumber);
    }

}

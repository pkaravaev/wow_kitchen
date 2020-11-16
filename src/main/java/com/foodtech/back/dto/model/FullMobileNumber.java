package com.foodtech.back.dto.model;

import com.foodtech.back.util.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static java.util.Objects.nonNull;

@Getter
@NoArgsConstructor
@NotNull
@EqualsAndHashCode
public class FullMobileNumber {

    @NotEmpty
    private String countryCode;

    @NotEmpty
    private String mobileNumber;

    public FullMobileNumber(String countryCode, String mobileNumber) {
        setCountryCode(countryCode);
        setMobileNumber(mobileNumber);
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = nonNull(countryCode) ? StringUtil.removeNonDigits(countryCode) : "";
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = nonNull(mobileNumber) ? StringUtil.removeNonDigits(mobileNumber) : "";
    }

    @Override
    public String toString() {
        return "+" + countryCode + mobileNumber;
    }
}

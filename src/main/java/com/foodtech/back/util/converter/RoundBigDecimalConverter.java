package com.foodtech.back.util.converter;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundBigDecimalConverter extends StdConverter<BigDecimal, BigDecimal> {
    @Override
    public BigDecimal convert(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP);
    }
}

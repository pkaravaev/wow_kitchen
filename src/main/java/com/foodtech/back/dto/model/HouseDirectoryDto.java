package com.foodtech.back.dto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseDirectoryDto {

    private String home;

    private BigDecimal latitude;

    private BigDecimal longitude;
}

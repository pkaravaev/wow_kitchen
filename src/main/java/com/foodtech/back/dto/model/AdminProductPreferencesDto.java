package com.foodtech.back.dto.model;

import lombok.Data;

import java.util.Set;

@Data
public class AdminProductPreferencesDto {

    private String productId;

    private String productName;

    private boolean vegetarian;

    private boolean spicy;

    private boolean withNuts;

    private Set<String> productPreferenceKeyWords;

}

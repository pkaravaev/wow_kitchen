package com.foodtech.back.dto.iiko;

import lombok.Data;

@Data
public class IikoOrderItemModifierDto {

    private String id; // ID продукта-модификатора

    private String name;

    private int amount; // количество
}

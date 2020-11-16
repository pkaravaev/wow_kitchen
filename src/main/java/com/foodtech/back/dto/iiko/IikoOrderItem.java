package com.foodtech.back.dto.iiko;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IikoOrderItem {

    private String id;

    private String name;

    private String code;

    private int amount; // количество

    private int sum; // стоимость

    private List<IikoOrderItemModifierDto> modifiers;

    private String comment; // max 255

    public IikoOrderItem(String id, String name, String code, int amount, int sum) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.amount = amount;
        this.sum = sum;
    }
}

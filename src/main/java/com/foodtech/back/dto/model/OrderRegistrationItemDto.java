package com.foodtech.back.dto.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Data
public class OrderRegistrationItemDto {

    @NotEmpty
    private String productId;

    @Min(1)
    private int amount;
}

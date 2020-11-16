package com.foodtech.back.dto.payment.cloud;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class BindCardDto {

    @NotEmpty
    private String cardCryptogramPacket;
}

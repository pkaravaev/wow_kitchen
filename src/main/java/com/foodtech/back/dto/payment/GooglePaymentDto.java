package com.foodtech.back.dto.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper = false)
public class GooglePaymentDto extends PaymentDto {

    @NotEmpty
    private String googlePaymentData;

}

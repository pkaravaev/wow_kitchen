package com.foodtech.back.dto.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.foodtech.back.entity.model.Address;
import com.foodtech.back.entity.payment.BankCard;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@JsonInclude(NON_NULL)
public class UserInfoDto {

    private FullMobileNumber fullNumber;

    private String name;

    private Address address;

    private BankCard bankCard;

    private String registrationPromoCode;

    private int bonusAmount;

}

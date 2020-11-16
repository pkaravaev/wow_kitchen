package com.foodtech.back.dto.auth;

import com.foodtech.back.entity.model.Address;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class CredentialsAuthDto extends AuthDto {

    @NotEmpty
    private String smsCode;

    @NotNull
    @Valid
    private Address address;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

    public DeliveryCoordinate getCoordinates() {
        return DeliveryCoordinate.of(latitude, longitude);
    }

}

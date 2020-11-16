package com.foodtech.back.dto.iiko;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class IikoDeliveryCoordinateDto {

    private BigDecimal latitude;

    private BigDecimal longitude;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int locationOrder;

}

package com.foodtech.back.dto.iiko;

import lombok.Data;

import java.util.List;

@Data
public class IikoDeliveryZoneDto {

    private String name;

    private List<IikoDeliveryCoordinateDto> coordinates;
}

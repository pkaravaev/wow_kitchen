package com.foodtech.back.dto.iiko;

import lombok.Data;

import java.util.List;

@Data
public class IikoDeliveryRestrictionsDto {

    private List<IikoDeliveryZoneDto> deliveryZones;
}

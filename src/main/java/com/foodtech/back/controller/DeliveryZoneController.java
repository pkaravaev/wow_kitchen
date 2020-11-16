package com.foodtech.back.controller;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.model.AddressDirectoryDto;
import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import com.foodtech.back.entity.model.iiko.DeliveryZone;
import com.foodtech.back.service.model.AddressDirectoryService;
import com.foodtech.back.service.model.DeliveryZoneService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

import static com.foodtech.back.util.ControllerUtil.errorResponse;
import static com.foodtech.back.util.ControllerUtil.okResponse;
import static com.foodtech.back.util.ResponseCode.COORDINATES_NOT_IN_DELIVERY_ZONE;

@RestController
@RequestMapping(path = "/app/public/zone")
@Validated
public class DeliveryZoneController {

    private final DeliveryZoneService deliveryZoneService;

    private final AddressDirectoryService addressDirectoryService;

    private final ResourcesProperties properties;

    public DeliveryZoneController(DeliveryZoneService deliveryZoneService, AddressDirectoryService addressDirectoryService,
                                  ResourcesProperties properties) {
        this.deliveryZoneService = deliveryZoneService;
        this.addressDirectoryService = addressDirectoryService;
        this.properties = properties;
    }

    @GetMapping
    public JsonResponse<List<DeliveryZone>> get() {
        List<DeliveryZone> deliveryZones = deliveryZoneService.getDeliveryZones();
        return okResponse(deliveryZones);
    }

    @GetMapping(path = "/coordinates")
    public JsonResponse coordinateInZone(@RequestParam @NotEmpty String latitude, @RequestParam @NotEmpty String longitude) {
        boolean inZone = deliveryZoneService.coordinatesInDeliveryZone(DeliveryCoordinate.of(new BigDecimal(latitude),
                new BigDecimal(longitude)));
        return inZone ? okResponse() : errorResponse(COORDINATES_NOT_IN_DELIVERY_ZONE, properties.getCoordinatesInvalidMsg());
    }

    @GetMapping(path = "/directory")
    public JsonResponse getStreetDirectory() {
        List<AddressDirectoryDto> addressDirectory = addressDirectoryService.getAllInActiveZones();
        return okResponse(addressDirectory);
    }
}

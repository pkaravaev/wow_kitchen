package com.foodtech.back.controller;

import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.dto.properties.OrganizationInfoDto;
import com.foodtech.back.service.properties.PropertiesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.foodtech.back.service.properties.PropertiesService.*;
import static com.foodtech.back.util.ControllerUtil.okResponse;

@RestController
public class PropertiesController {

    private final PropertiesService propertiesService;

    public PropertiesController(PropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    @GetMapping(path = "/app/public/organization/info")
    public JsonResponse getOrganizationInfo() {
        Map<String, String> properties = propertiesService.getByNames(List.of(ORGANIZATION_NAME,
                ORGANIZATION_EMAIL, ORGANIZATION_SITE));
        OrganizationInfoDto infoDto = new OrganizationInfoDto(properties.get(ORGANIZATION_NAME),
                properties.get(ORGANIZATION_EMAIL), properties.get(ORGANIZATION_SITE));
        return okResponse(infoDto);
    }
}

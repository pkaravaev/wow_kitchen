package com.foodtech.back.controller;

import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.entity.model.iiko.ProductCategory;
import com.foodtech.back.security.JwtUser;
import com.foodtech.back.service.model.DeliveryZoneService;
import com.foodtech.back.service.model.ProductService;
import com.foodtech.back.util.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.foodtech.back.util.ControllerUtil.okResponse;

@RestController
@RequestMapping
@Slf4j
public class ProductController {

    private final ProductService productService;

    private final DeliveryZoneService deliveryZoneService;

    public ProductController(ProductService productService, DeliveryZoneService deliveryZoneService) {
        this.productService = productService;
        this.deliveryZoneService = deliveryZoneService;
    }

    @GetMapping(path = "/app/products")
    public JsonResponse getProducts(@AuthenticationPrincipal JwtUser jwtUser) {
        log.debug("Processing get products for app request");
        deliveryZoneService.checkKitchenIsOpenedOrElseThrow(UserMapper.toUser(jwtUser));
        List<ProductCategory> products = productService.getForFrontApp(jwtUser.getId());
        return okResponse(products);
    }
}

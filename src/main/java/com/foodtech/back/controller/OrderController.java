package com.foodtech.back.controller;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.dto.model.OrderInfoDto;
import com.foodtech.back.dto.model.OrderRegistrationDto;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.OrderStatus;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.security.JwtUser;
import com.foodtech.back.service.model.DeliveryZoneService;
import com.foodtech.back.service.model.OrderService;
import com.foodtech.back.util.mapper.UserMapper;
import com.foodtech.back.util.ControllerUtil;
import com.foodtech.back.util.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.foodtech.back.util.ControllerUtil.okResponse;

@RestController
@RequestMapping("/app/order")
@Slf4j
@Validated
public class OrderController {

    private final OrderService orderService;

    private final DeliveryZoneService deliveryZoneService;

    private final ResourcesProperties properties;

    public OrderController(OrderService orderService,
                           DeliveryZoneService deliveryZoneService, ResourcesProperties properties) {
        this.orderService = orderService;
        this.deliveryZoneService = deliveryZoneService;
        this.properties = properties;
    }

    @GetMapping
    public JsonResponse getAll(@AuthenticationPrincipal JwtUser jwtUser) {

        User user = UserMapper.toUser(jwtUser);
        log.info("Getting order history for user '{}'", user);
        return ControllerUtil.okResponse(OrderMapper.toOrderDtoList(orderService.getAllByUserWithItemsAndAddress(user)));
    }

    @GetMapping(path = "/{orderId}")
    public JsonResponse get(@PathVariable Long orderId, @AuthenticationPrincipal JwtUser jwtUser) {

        log.info("Getting order '{}' info  for user '{}'", orderId, jwtUser);
        Order order = orderService.getByIdAndUserWithItemsAndAddress(orderId, jwtUser.getId()).orElseThrow();
        return ControllerUtil.okResponse(OrderMapper.toDto(order));
    }

    @GetMapping(path = "/processing")
    public JsonResponse getInProcessing(@AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Get order in processing for user '{}' request", jwtUser);
        Order inProcessing = orderService.getUserOrderInProcessing(jwtUser.getId()).orElse(null);
        return ControllerUtil.okResponse(OrderMapper.toProcessingDto(inProcessing, properties.getRabbitMqHost()));
    }

    @GetMapping(path = "/{orderId}/status")
    public JsonResponse getStatus(@PathVariable Long orderId, @AuthenticationPrincipal JwtUser jwtUser) {

        log.info("Getting order '{}' status for user '{}'", orderId, jwtUser);
        OrderStatus status = orderService.getStatusByIdAndUserId(orderId, jwtUser.getId());
        return ControllerUtil.okResponse(status.toString());
    }

    @PostMapping
    public JsonResponse register(@Valid @RequestBody OrderRegistrationDto orderDto, @AuthenticationPrincipal JwtUser jwtUser) {

        log.info("Registering new order for user '{}'", jwtUser);
        deliveryZoneService.checkKitchenIsOpenedOrElseThrow(UserMapper.toUser(jwtUser));
        Order order = orderService.register(jwtUser.getId(), orderDto);
        log.info("Order '{}' for user '{}' successfully registered", order.getId(), jwtUser);
        return ControllerUtil.okResponse(new OrderInfoDto(order.getId(), properties.getRabbitMqHost(), order.getStatusQueueName(), order.getDeliveryTime()));
    }
}

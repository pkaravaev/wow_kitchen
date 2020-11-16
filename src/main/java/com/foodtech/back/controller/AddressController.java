package com.foodtech.back.controller;

import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.entity.model.Address;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import com.foodtech.back.security.JwtUser;
import com.foodtech.back.service.model.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

import static com.foodtech.back.util.ControllerUtil.okResponse;
import static com.foodtech.back.util.mapper.UserMapper.toUser;

@RestController
@RequestMapping("/app/user")
@Slf4j
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping(path = "/addresses")
    public JsonResponse<List<Address>> getAddresses(@AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Get addresses request for user '{}'", jwtUser);
        List<Address> addresses = addressService.getAllByUser(jwtUser.getId());
        return okResponse(addresses);
    }

    @PostMapping(path = "/address")
    public JsonResponse addNewAddress(@Valid @RequestBody Address newAddress, @RequestParam String latitude,
                                      @RequestParam String longitude, @AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Add new address for user '{}' request", jwtUser);
        Address addedAddress = addressService.add(toUser(jwtUser), newAddress,
                DeliveryCoordinate.of(new BigDecimal(latitude), new BigDecimal(longitude)));
        return okResponse(addedAddress);
    }

    @PostMapping(path = "/address/actual/{addressId}")
    public JsonResponse setActualAddress(@PathVariable Long addressId, @AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Set another actual address for user '{}' request", jwtUser);
        Address actualAddress = addressService.setActual(jwtUser.getId(), addressId);
        return okResponse(actualAddress);
    }
}

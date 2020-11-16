package com.foodtech.back.controller;

import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.dto.payment.cloud.BindCardDto;
import com.foodtech.back.dto.payment.cloud.CloudPaymentTransactionResultDto;
import com.foodtech.back.security.JwtUser;
import com.foodtech.back.service.payment.cloud.CloudPaymentCardBindingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.foodtech.back.util.ControllerUtil.okResponse;
import static com.foodtech.back.util.mapper.UserMapper.toUser;

@RestController
@Slf4j
public class CloudPaymentCardBindingController {

    private final CloudPaymentCardBindingService cardBindingService;

    public CloudPaymentCardBindingController(CloudPaymentCardBindingService cardBindingService) {
        this.cardBindingService = cardBindingService;
    }

    @PostMapping(path = "/app/cloud/payment/bind")
    public JsonResponse bind(@Valid @RequestBody BindCardDto bindCardDto, @AuthenticationPrincipal JwtUser jwtUser) {
        log.info("New bank card binding request. User: '{}'", jwtUser);
        CloudPaymentTransactionResultDto result = cardBindingService.bindOrForm3DSParams(toUser(jwtUser), bindCardDto);
        log.info("New bank card bind request successfully processed. 3DS required: {}. User: '{}'", result.isNeed3DS(), jwtUser);
        return okResponse(result);
    }


    @PostMapping(path = "/app/public/cloud/bind/threeDsCallback")
    public void acs3DSCallback(@RequestParam(name = "PaRes") String paRes,
                               @RequestParam(name = "MD") String merchantData) {
        log.info("Received card binding 3DS callback");
        cardBindingService.completeCardBindingAfter3DSCallback(paRes, merchantData);
    }
}

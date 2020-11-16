package com.foodtech.back.controller;

import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.dto.payment.GooglePaymentDto;
import com.foodtech.back.dto.payment.PaymentDto;
import com.foodtech.back.dto.payment.cloud.CloudPaymentTransactionResultDto;
import com.foodtech.back.security.JwtUser;
import com.foodtech.back.service.model.DeliveryZoneService;
import com.foodtech.back.service.payment.cloud.CloudPaymentService;
import com.foodtech.back.util.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.foodtech.back.util.ControllerUtil.errorResponse;
import static com.foodtech.back.util.ControllerUtil.okResponse;
import static com.foodtech.back.util.ResponseCode.PAYMENT_FAILED;
import static com.foodtech.back.util.mapper.UserMapper.toUser;

@SuppressWarnings("rawtypes")
@RestController
@Slf4j
public class CloudPaymentController {

    private final CloudPaymentService paymentService;

    private final DeliveryZoneService deliveryZoneService;

    public CloudPaymentController(CloudPaymentService paymentService,
                                  DeliveryZoneService deliveryZoneService) {
        this.paymentService = paymentService;
        this.deliveryZoneService = deliveryZoneService;
    }

    @PostMapping(path = "/app/order/payment")
    public JsonResponse payWithCardToken(@Valid @RequestBody PaymentDto paymentDto, @AuthenticationPrincipal JwtUser jwtUser) {

        Long orderId = paymentDto.getOrderId();
        deliveryZoneService.checkKitchenIsOpenedOrElseThrow(UserMapper.toUser(jwtUser));
        log.info("Card payment request. Order: '{}'. User: '{}'", orderId, jwtUser);
        paymentService.authWithCardToken(toUser(jwtUser), orderId);
        log.info("Order '{}' for user '{}' successfully paid", orderId, jwtUser);
        return okResponse();
    }

    @PostMapping(path = "/app/order/payment/googlePay")
    public JsonResponse payWithGooglePay(@Valid @RequestBody GooglePaymentDto paymentDto, @AuthenticationPrincipal JwtUser jwtUser) {

        Long orderId = paymentDto.getOrderId();
        deliveryZoneService.checkKitchenIsOpenedOrElseThrow(UserMapper.toUser(jwtUser));
        log.info("GooglePay payment request of order '{}' for user '{}'", orderId, jwtUser);
        CloudPaymentTransactionResultDto result = paymentService.authWithGooglePay(toUser(jwtUser), orderId,
                paymentDto.getGooglePaymentData());
        return okResponse(result);
    }

    @PostMapping(path = "/app/public/cloud/payment/threeDsCallback")
    public void acs3DSCallback(@RequestParam(name = "PaRes") String paRes,
                               @RequestParam(name = "MD") String merchantData) {

        log.info("Received payment 3DS callback");
        paymentService.completePaymentAfter3DSCallback(paRes, merchantData);
    }

    /* Тест доступности API CloudPayment */
    @GetMapping(path = "/app/cloud/payment/test")
    public JsonResponse testRequest(@AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Cloud payment test request for user '{}'", jwtUser);
        return paymentService.testCloudPayment() ? okResponse() : errorResponse(PAYMENT_FAILED, "Test failed");
    }

    /* Тест примененных пропертей */
    @GetMapping(path = "/app/public/cloud/payment/test/properties")
    public String testPropertiesProfile() {
        return paymentService.testPaymentProperties();
    }
}

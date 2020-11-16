package com.foodtech.back.controller;

import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.security.JwtUser;
import com.foodtech.back.service.bonus.BonusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static com.foodtech.back.util.ControllerUtil.okResponse;

@RestController
@RequestMapping(path = "/app/bonus")
@Slf4j
@Validated
public class BonusController {

    public static final int PROMO_CODE_MIN_LENGTH = 5;
    public static final int PROMO_CODE_MAX_LENGTH = 10;

    private final BonusService bonusService;

    public BonusController(BonusService bonusService) {
        this.bonusService = bonusService;
    }

    @PostMapping(path = "/{promoCode}")
    public JsonResponse applyPromoCode(@PathVariable @Size(min = PROMO_CODE_MIN_LENGTH, max = PROMO_CODE_MAX_LENGTH) String promoCode,
                                       @AuthenticationPrincipal JwtUser jwtUser) {

        log.info("Applying promo code '{}' for user '{}'", promoCode, jwtUser);
        Integer appliedBonusAmount = bonusService.applyPromoCode(promoCode, jwtUser.getId());
        return okResponse(appliedBonusAmount);
    }

    @PutMapping(path = "/{promoCode}")
    public JsonResponse changePromoCode(@PathVariable @NotBlank @Size(min = 5, max = 10) String promoCode,
                                        @AuthenticationPrincipal JwtUser jwtUser) {

        log.info("Changing promo code for user '{}'", jwtUser);
        bonusService.changeRegistrationPromoCode(promoCode, jwtUser.getId());
        return okResponse();
    }
}

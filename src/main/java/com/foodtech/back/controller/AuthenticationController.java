package com.foodtech.back.controller;

import com.foodtech.back.dto.auth.AuthDto;
import com.foodtech.back.dto.auth.CredentialsDto;
import com.foodtech.back.dto.auth.RefreshCredentialsDto;
import com.foodtech.back.dto.auth.CredentialsAuthDto;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.service.auth.AuthenticationService;
import com.foodtech.back.util.exceptions.MobileNumberInvalidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.foodtech.back.util.ControllerUtil.okResponse;
import static com.foodtech.back.util.ValidationUtil.formMobileNumber;
import static com.foodtech.back.util.ValidationUtil.mobileNumberIsValid;

@RestController
@RequestMapping("/app/public/auth")
@Slf4j
@Validated
public class AuthenticationController {

    private final AuthenticationService authService;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/sms")
    public JsonResponse sendAuthCredentials(@Valid @RequestBody AuthDto authDto) {

        FullMobileNumber fullNumber = authDto.getFullNumber();
        validateMobileNumber(fullNumber);
        authService.sendAuthCredentials(authDto);
        return okResponse();
    }

    @PutMapping(path = "/sms")
    public JsonResponse authenticate(@Valid @RequestBody CredentialsAuthDto credentialsAuthDto) {

        FullMobileNumber fullNumber = credentialsAuthDto.getFullNumber();
        validateMobileNumber(fullNumber);
        log.info("Authenticate request. User: '{}'", fullNumber);
        CredentialsDto tokens = authService.authenticate(credentialsAuthDto);
        return okResponse(tokens);
    }

    @PostMapping(path = "/token")
    public JsonResponse refreshCredentials(@Valid @RequestBody RefreshCredentialsDto credentialsDto) {

        log.info("Refresh token request. User: '{}'", credentialsDto.getFullNumber());
        CredentialsDto refreshedTokens = authService.refreshCredentials(credentialsDto);
        log.info("Completed refresh token request. User: '{}'", credentialsDto.getFullNumber());
        return okResponse(refreshedTokens);
    }

    private void validateMobileNumber(FullMobileNumber fullNumber) {
        formMobileNumber(fullNumber);
        if (!mobileNumberIsValid(fullNumber)) {
            throw new MobileNumberInvalidException(fullNumber.getMobileNumber());
        }
    }
}

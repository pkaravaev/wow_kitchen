package com.foodtech.back.controller;

import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.dto.model.UserInfoDto;
import com.foodtech.back.dto.model.UserUpdateDto;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.model.UserPreferences;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.security.JwtUser;
import com.foodtech.back.service.model.BankCardServiceImpl;
import com.foodtech.back.service.model.UserPreferencesService;
import com.foodtech.back.service.model.UserService;
import com.foodtech.back.service.notification.push.FirebaseTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static com.foodtech.back.util.ControllerUtil.okResponse;
import static com.foodtech.back.util.mapper.UserMapper.toUser;
import static com.foodtech.back.util.mapper.UserMapper.toUserDto;

@RestController
@RequestMapping("/app/user")
@Slf4j
public class UserController {

    private final UserService userService;

    private final BankCardServiceImpl bankCardService;

    private final UserPreferencesService userPreferencesService;

    private final FirebaseTokenService firebaseTokenService;

    public UserController(UserService userService, BankCardServiceImpl bankCardService,
                          UserPreferencesService userPreferencesService, FirebaseTokenService firebaseTokenService) {
        this.userService = userService;
        this.bankCardService = bankCardService;
        this.userPreferencesService = userPreferencesService;
        this.firebaseTokenService = firebaseTokenService;
    }

    @GetMapping
    public JsonResponse<UserInfoDto> getProfile(@AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Get profile request for user '{}'", jwtUser);
        User user = userService.getWithActualAddress(jwtUser.getId());
        BankCard bankCard = bankCardService.getActualCard(jwtUser.getId()).orElse(null);
        return okResponse(toUserDto(user, bankCard));
    }

    @PostMapping
    public JsonResponse<UserInfoDto> update(@Valid @RequestBody UserUpdateDto userDto, @AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Updating user data '{}'", jwtUser);
        User user = userService.update(jwtUser.getId(), userDto);
        return okResponse(toUserDto(user, null));
    }

    @GetMapping(path = "/preferences")
    public JsonResponse getPreferences(@AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Getting user preferences for '{}'", jwtUser);
        UserPreferences preferences = userPreferencesService.findByUserId(jwtUser.getId()).orElseThrow();
        return okResponse(preferences);
    }

    @PostMapping(path = "/preferences")
    public JsonResponse updatePreferences(@Valid @RequestBody UserPreferences preferences,
                                          @AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Updating preferences for user '{}'", jwtUser);
        UserPreferences updated = userPreferencesService.updatePreferences(preferences, jwtUser.getId());
        return okResponse(updated);
    }

    @GetMapping(path = "/preferences/words")
    public JsonResponse getKeyWords() {
        return okResponse(userPreferencesService.getAllKeyWordsForApp());
    }

    @PostMapping(path = "/firebase")
    public JsonResponse setFirebaseToken(@RequestParam @NotBlank String token, @AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Updating firebase token for user '{}'", jwtUser);
        firebaseTokenService.setToken(toUser(jwtUser), token);
        return okResponse();
    }
}

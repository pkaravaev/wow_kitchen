package com.foodtech.back.controller;

import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.security.JwtUser;
import com.foodtech.back.service.model.BankCardServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.foodtech.back.util.ControllerUtil.okResponse;

@RestController
@RequestMapping("/app/user")
@Slf4j
public class BankCardController {

    private final BankCardServiceImpl bankCardService;

    public BankCardController(BankCardServiceImpl bankCardService) {
        this.bankCardService = bankCardService;
    }

    @GetMapping(path = "/cards")
    public JsonResponse<List<BankCard>> getBankCards(@AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Get bank cards request for user '{}'", jwtUser);
        List<BankCard> bankCards = bankCardService.getCards(jwtUser.getId());
        return okResponse(bankCards);
    }

    @GetMapping(path = "/cards/actual")
    public JsonResponse<BankCard> getActualBankCard(@AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Get actual bank card request for user '{}'", jwtUser);
        return okResponse(bankCardService.getActualCard(jwtUser.getId()).orElse(null));
    }

    @PostMapping(path = "/card/actual/{cardId}")
    public JsonResponse setActualCard(@PathVariable Long cardId, @AuthenticationPrincipal JwtUser jwtUser) {
        log.info("Set another actual card for user '{}' request", jwtUser);
        BankCard actualCard = bankCardService.setActual(jwtUser.getId(), cardId);
        return okResponse(actualCard);
    }

}

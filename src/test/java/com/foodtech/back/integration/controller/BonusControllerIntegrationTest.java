package com.foodtech.back.integration.controller;

import com.foodtech.back.controller.BonusController;
import com.foodtech.back.entity.bonus.BonusAccount;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.service.bonus.BonusService;
import com.foodtech.back.util.ResponseCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static com.foodtech.back.IntegrationTestData.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"classpath:user-data.sql", "classpath:bonus-data.sql"})
class BonusControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Test
    void applyRegistrationPromoCode() throws Exception {

        mockMvc.perform(post("/app/bonus/" + USER_2_REG_PROMO_CODE)
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertBonusAmountApplied(BonusService.REGISTRATION_PROMO_CODE_BONUS_AMOUNT);
    }

    @Test
    void applyImpersonalPromoCode() throws Exception {

        mockMvc.perform(post("/app/bonus/" + IMPERSONAL_PROMO_CODE)
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertBonusAmountApplied(IMPERSONAL_PROMO_CODE_AMOUNT);
    }

    private void assertBonusAmountApplied(int appliedAmount) {
        User user = userRepository.findById(testData.user1().getId()).orElseThrow();
        BonusAccount bonusAccount = user.getBonusAccount();
        int expectedAmount = testData.user1().getBonusAccount().getBonusAmount() + appliedAmount;
        assertEquals(expectedAmount, bonusAccount.getBonusAmount());
    }

    @Test
    void applyPromoCodeInvalid() throws Exception {

        mockMvc.perform(post("/app/bonus/" + "A".repeat(BonusController.PROMO_CODE_MAX_LENGTH + 1))
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.REQUEST_INVALID.toString()));

        assertBonusAmountNotChanged();
    }

    @Test
    void applySelfPromoCode() throws Exception {

        mockMvc.perform(post("/app/bonus/" + USER_1_REG_PROMO_CODE)
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.OWN_REG_PROMO_CODE.toString()));

        assertBonusAmountNotChanged();
    }

    private void assertBonusAmountNotChanged() {
        User user = userRepository.findById(testData.user1().getId()).orElseThrow();
        BonusAccount bonusAccount = user.getBonusAccount();
        assertEquals(testData.user1().getBonusAccount().getBonusAmount(), bonusAccount.getBonusAmount());
    }

    @Test
    void changeRegistrationPromoCode() throws Exception {

        String newCode = "NewCode";

        /* Меняем промо-код */
        mockMvc.perform(put("/app/bonus/" + newCode)
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertRegistrationPromoCodeChanged(newCode);
    }

    private void assertRegistrationPromoCodeChanged(String newCode) {
        User user = userRepository.findById(testData.user1().getId()).orElseThrow();
        assertEquals(newCode, user.getBonusAccount().getRegistrationPromoCode());
    }

    @Test
    void changeRegistrationPromoCodeInvalid() throws Exception {

        String tooLongCode = "A".repeat(BonusController.PROMO_CODE_MAX_LENGTH + 1);
        mockMvc.perform(put("/app/bonus/" + tooLongCode)
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.REQUEST_INVALID.toString()));

        assertPromoCodeNotChanged();
    }

    @Test
    void changeRegistrationPromoCodeAlreadyExists() throws Exception {

        mockMvc.perform(put("/app/bonus/" + USER_2_REG_PROMO_CODE)
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.REGISTRATION_PROMO_CODE_EXISTS.toString()));

        assertPromoCodeNotChanged();
    }

    private void assertPromoCodeNotChanged() {
        User user = userRepository.findById(testData.user1().getId()).orElseThrow();
        assertEquals(testData.user1().getBonusAccount().getRegistrationPromoCode(), user.getBonusAccount().getRegistrationPromoCode());
    }
}
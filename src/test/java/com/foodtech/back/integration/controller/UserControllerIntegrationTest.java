package com.foodtech.back.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.foodtech.back.dto.model.UserInfoDto;
import com.foodtech.back.dto.model.UserUpdateDto;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.model.UserPreferences;
import com.foodtech.back.repository.model.UserPreferencesRepository;
import com.foodtech.back.util.ResponseCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"classpath:user-data.sql"})
class UserControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Autowired
    UserPreferencesRepository userPreferencesRepository;

    @Test
    void getProfile() throws Exception {

        MvcResult result = mockMvc.perform(get("/app/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        UserInfoDto userDto = mapResult(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(testData.user1().getName(), userDto.getName());
        assertEquals(testData.user1().getBonusAccount().getBonusAmount(), userDto.getBonusAmount());
        assertEquals(testData.user1().getBonusAccount().getRegistrationPromoCode(), userDto.getRegistrationPromoCode());
        assertEquals(testData.user1().getFullMobileNumber(), userDto.getFullNumber());
        assertEquals(testData.address1(), userDto.getAddress());
        assertEquals(testData.bankCard1().getCardMask(), userDto.getBankCard().getCardMask());
        assertEquals(testData.bankCard1().getCardType(), userDto.getBankCard().getCardType());
    }

    @Test
    void getProfileUnauthorized() throws Exception {

        mockMvc.perform(get("/app/user")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.UNAUTHORIZED.toString()));
    }

    @Test
    void updateProfile() throws Exception {

        String updatedName = "Updated Name";
        checkUpdateProfileTestData(updatedName);

        mockMvc.perform(post("/app/user")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserUpdateDto(updatedName))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.body.name").value(updatedName));

        assertNameUpdatedSaved(updatedName);
    }

    void checkUpdateProfileTestData(String updatedName) {
        User user = userRepository.findById(testData.user1().getId()).orElseThrow();
        assertNotEquals(updatedName, user.getName());
    }

    void assertNameUpdatedSaved(String updatedName) {
        User user = userRepository.findById(testData.user1().getId()).orElseThrow();
        assertEquals(updatedName, user.getName());
    }

    @Test
    void updateProfileUnauthorized() throws Exception {

        String updatedName = "Updated name";

        mockMvc.perform(post("/app/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserUpdateDto(updatedName))))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.UNAUTHORIZED.toString()));


        assertNameNotUpdated(updatedName);
    }

    void assertNameNotUpdated(String updatedName) {
        User user = userRepository.findById(testData.user1().getId()).orElseThrow();
        assertNotEquals(updatedName, user.getName());
    }

    @Test
    void getPreferences() throws Exception {

        MvcResult result = mockMvc.perform(get("/app/user/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        UserPreferences userPreferences = mapResult(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertNotNull(userPreferences);
        assertThat(userPreferences).isEqualToIgnoringGivenFields(testData.user1PreferencesData(), "user", "id");
    }

    @Test
    void updatePreferences() throws Exception {

        checkUpdatePreferencesTestData();

        mockMvc.perform(post("/app/user/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader())
                .content(objectMapper.writeValueAsString(testData.updatedUser1PreferencesData())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertPreferencesUpdated();
    }

    private void checkUpdatePreferencesTestData() {
        UserPreferences preferences = userPreferencesRepository.findByUserId(testData.user1().getId()).orElseThrow();
        assertNotEquals(preferences.isWithoutNuts(), testData.updatedUser1PreferencesData().isWithoutNuts());
        assertNotEquals(preferences.isVegetarian(), testData.updatedUser1PreferencesData().isVegetarian());
        assertNotEquals(preferences.isNotSpicy(), testData.updatedUser1PreferencesData().isNotSpicy());
    }

    private void assertPreferencesUpdated() {
        UserPreferences preferences = userPreferencesRepository.findByUserId(testData.user1().getId()).orElseThrow();
        assertThat(preferences).isEqualToIgnoringGivenFields(testData.updatedUser1PreferencesData(), "user", "created", "updated", "dontLike");
        assertThat(preferences.getDontLike()).containsExactlyElementsOf(testData.updatedUser1PreferencesData().getDontLike());
    }

    @Test
    void updatePreferencesWrongKeyWord() throws Exception {

        UserPreferences updatedPreferences = testData.updatedUser1PreferencesData();
        Set<String> wrongDontLikeSet = Set.of("Key-word-not-presented-in-DB");
        updatedPreferences.setDontLike(wrongDontLikeSet);

        mockMvc.perform(post("/app/user/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader())
                .content(objectMapper.writeValueAsString(updatedPreferences)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        assertPreferencesDoesNotChanged(wrongDontLikeSet);
    }

    private void assertPreferencesDoesNotChanged(Set<String> wrongDontLikeSet) {
        UserPreferences preferences = userPreferencesRepository.findByUserId(testData.user1().getId()).orElseThrow();
        assertThat(preferences.getDontLike()).doesNotContainAnyElementsOf(wrongDontLikeSet);
    }
}
package com.foodtech.back.integration.controller;

import com.foodtech.back.dto.auth.AuthDto;
import com.foodtech.back.dto.auth.CredentialsAuthDto;
import com.foodtech.back.dto.auth.RefreshCredentialsDto;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.entity.auth.SmsAuth;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.repository.auth.SmsAuthRepository;
import com.foodtech.back.service.notification.sms.SmsMessageDto;
import com.foodtech.back.service.notification.sms.SmsSender;
import com.foodtech.back.util.ResponseCode;
import com.foodtech.back.util.SmsUtil;
import com.foodtech.back.util.StringUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;

import static com.foodtech.back.IntegrationTestData.*;
import static com.foodtech.back.service.auth.SmsCodeCredentialsChecker.MAX_SMS_ATTEMPTS;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"classpath:user-data.sql"})
class AuthControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @MockBean
    private SmsSender smsSender;

    @MockBean
    private SmsUtil smsUtil;

    @Autowired
    private SmsAuthRepository smsAuthRepository;

    @BeforeEach
    void beforeTest() {
        when(smsSender.send(any())).thenReturn(new SmsMessageDto(true));
        when(smsUtil.generateAuthCode()).thenReturn(SMS_CODE);
    }

    @Test
    void publicAccess() throws Exception {
        MvcResult response = mockMvc.perform(get("/app/public"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("Hello Unsecured World!", response.getResponse().getContentAsString());
    }

    @Test
    void authSuccess() throws Exception {

        MvcResult response = mockMvc.perform(get("/app")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("Hello Secured World!", response.getResponse().getContentAsString());
    }

    @Test
    void authTokenInvalid() throws Exception {

        MvcResult response = mockMvc.perform(get("/app")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer some_invalid_token_value"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String jsonContent = response.getResponse().getContentAsString();
        assertTrue(jsonContent.contains("TOKEN_INVALID"));
    }

    @Test
    void sendSms() throws Exception {

        mockMvc.perform(post("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDto(AUTH_NEW_USER_FULL_MOBILE_NUMBER))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertSmsAuthSaved();
    }

    void assertSmsAuthSaved() {
        SmsAuth smsAuth = getTestSmsAuth();
        assertNotNull(smsAuth);
        assertNotNull(smsAuth.getSmsCode());
        assertEquals(AUTH_NEW_USER_MOBILE_NUMBER, smsAuth.getMobileNumber());
        assertFalse(smsAuth.getUsed());
    }

    @Test
    void sendSmsMobileInvalid() throws Exception {

        mockMvc.perform(post("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDto(INVALID_FULL_MOBILE_NUMBER))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.MOB_NUM_INVALID.toString()));

        /* Проверяем, что смс не отсылалось */
        verify(smsSender, times(0)).send(any(SmsMessageDto.class));
    }

    @Test
    @Disabled
    /* В настоящий момент нет ограничения на отправку смс одному юзеру */
    void sendSmsDelay() throws Exception {

        mockMvc.perform(post("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDto(USER_1_FULL_MOBILE_NUMBER))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDto(USER_1_FULL_MOBILE_NUMBER))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.SMS_DELAY.toString()));
    }

    @Test
    void checkSmsSuccessNewUser() throws Exception {

        checkSmsSuccessNewUserTestData();

        /* Запрос на отправку смс кода */
        mockMvc.perform(post("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDto(AUTH_NEW_USER_FULL_MOBILE_NUMBER))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        /* Запрос на проверку смс кода */
        mockMvc.perform(put("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.smsAuthNewUserFromFrontData())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ResponseCode.OK.toString()))
                .andExpect(jsonPath("$.body.authToken").isNotEmpty())
                .andExpect(jsonPath("$.body.refreshToken").isNotEmpty());

        assertUserCreated();
    }

    void checkSmsSuccessNewUserTestData() {
        Optional<User> userOpt = userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, AUTH_NEW_USER_MOBILE_NUMBER);
        assertTrue(userOpt.isEmpty());
    }

    void assertUserCreated() {
        User newUser = userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, AUTH_NEW_USER_MOBILE_NUMBER).orElseThrow();
        assertNotNull(newUser);
        assertEquals(AUTH_NEW_USER_MOBILE_NUMBER, newUser.getMobileNumber());
    }

    @Test
    void checkSmsSuccessExistingUser() throws Exception {

        /* Запрос на отправку смс кода */
        mockMvc.perform(post("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDto(USER_2_FULL_MOBILE_NUMBER))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        /* Запрос на проверку смс кода */
        mockMvc.perform(put("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.smsAuthExistingUserFromFrontData())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ResponseCode.OK.toString()))
                .andExpect(jsonPath("$.body.authToken").isNotEmpty())
                .andExpect(jsonPath("$.body.refreshToken").value(REFRESH_TOKEN_2));
    }

    @Test
    void checkSmsMobNumberInvalid() throws Exception {

        /* Ставим неверный номер */
        CredentialsAuthDto smsAuth = testData.smsAuthNewUserFromFrontData();
        FullMobileNumber number = new FullMobileNumber(COUNTRY_CODE, INVALID_MOBILE_NUMBER);
        smsAuth.setFullNumber(number);

        mockMvc.perform(put("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(smsAuth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.MOB_NUM_INVALID.toString()));

        assertUserNotCreated(number);
    }

    @Test
    void checkSmsInvalid() throws Exception {

        /* Запрос на отправку смс кода */
        mockMvc.perform(post("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDto(AUTH_NEW_USER_FULL_MOBILE_NUMBER))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        /* Ставим неверный код */
        CredentialsAuthDto smsAuth = testData.smsAuthNewUserFromFrontData();
        smsAuth.setSmsCode("0000");

        mockMvc.perform(put("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(smsAuth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.SMS_INVALID.toString()));

        assertInvalidAttemptSaved();
        assertUserNotCreated(AUTH_NEW_USER_FULL_MOBILE_NUMBER);
    }

    void assertInvalidAttemptSaved() {
        SmsAuth smsAuth = getTestSmsAuth();
        assertEquals(Integer.valueOf(1), smsAuth.getAttempt());
    }

    @Test
    void checkSmsMaxAttempt() throws Exception {

        /* Запрос на отправку смс кода */
        mockMvc.perform(post("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDto(AUTH_NEW_USER_FULL_MOBILE_NUMBER))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        /* Ставим неверный код */
        CredentialsAuthDto smsAuth = testData.smsAuthNewUserFromFrontData();
        smsAuth.setSmsCode("0000");

        /* Отправляем неправильные коды */
        for (int i = 0; i < MAX_SMS_ATTEMPTS-1; i++) {
            mockMvc.perform(put("/app/public/auth/sms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(smsAuth)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(ResponseCode.SMS_INVALID.toString()));

            SmsAuth auth = getTestSmsAuth();
            assertEquals(Integer.valueOf(i+1), auth.getAttempt());
        }

        mockMvc.perform(put("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(smsAuth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.SMS_MAX_ATTEMPT.toString()));

        assertMaxAttemptsSaved();
        assertUserNotCreated(AUTH_NEW_USER_FULL_MOBILE_NUMBER);
    }

    private void assertMaxAttemptsSaved() {
        SmsAuth auth = getTestSmsAuth();
        assertTrue(auth.getUsed());
        assertEquals(Integer.valueOf(MAX_SMS_ATTEMPTS), auth.getAttempt());
        assertUserNotCreated(AUTH_NEW_USER_FULL_MOBILE_NUMBER);
    }

    @Test
    void checkSmsUserAddressInvalid() throws Exception {

        /* Запрос на отправку смс кода */
        mockMvc.perform(post("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthDto(AUTH_NEW_USER_FULL_MOBILE_NUMBER))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        /* Ставим неверные координаты */
        CredentialsAuthDto authDto = testData.smsAuthNewUserFromFrontData();
        authDto.setLatitude(BigDecimal.ZERO);
        authDto.setLongitude(BigDecimal.ZERO);

        mockMvc.perform(put("/app/public/auth/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.USER_ADDRESS_INVALID.toString()));

        assertUserNotCreated(AUTH_NEW_USER_FULL_MOBILE_NUMBER);
    }

    private void assertUserNotCreated(FullMobileNumber number) {
        Optional<User> user = userRepository.findByCountryCodeAndMobileNumber(number.getCountryCode(), number.getMobileNumber());
        assertTrue(user.isEmpty());
    }

    @Test
    void authTokenExpired() throws Exception {

        String authToken = "Bearer " + testData.generateExpiredToken(new HashMap<>(),
                StringUtil.formFullMobileNumberStr(USER_2_FULL_MOBILE_NUMBER));

        MvcResult response = mockMvc.perform(get("/app")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authToken))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String jsonContent = response.getResponse().getContentAsString();
        assertTrue(jsonContent.contains("TOKEN_EXPIRED"));
    }

    @Test
    void authTokenNoAuthHeader() throws Exception {

        MvcResult response = mockMvc.perform(get("/app")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String jsonContent = response.getResponse().getContentAsString();
        assertTrue(jsonContent.contains("UNAUTHORIZED"));
    }

    @Test
    void refreshToken() throws Exception {

        String expiredToken = testData.generateExpiredToken(new HashMap<>(),
                StringUtil.formFullMobileNumberStr(USER_1_FULL_MOBILE_NUMBER));

        RefreshCredentialsDto refreshTokenDto = new RefreshCredentialsDto();
        refreshTokenDto.setFullNumber(USER_1_FULL_MOBILE_NUMBER);
        refreshTokenDto.setAuthToken(expiredToken);
        refreshTokenDto.setRefreshToken(REFRESH_TOKEN);

        mockMvc.perform(post("/app/public/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.authToken").isNotEmpty())
                .andExpect(jsonPath("$.body.refreshToken").value(REFRESH_TOKEN));
    }

    @Test
    void refreshTokenRefreshTokenInvalid() throws Exception {

        String expiredToken = testData.generateExpiredToken(new HashMap<>(),
                StringUtil.formFullMobileNumberStr(USER_1_FULL_MOBILE_NUMBER));

        RefreshCredentialsDto refreshTokenDto = new RefreshCredentialsDto();
        refreshTokenDto.setFullNumber(USER_1_FULL_MOBILE_NUMBER);
        refreshTokenDto.setAuthToken(expiredToken);
        refreshTokenDto.setRefreshToken("Invalid_refresh_token");

        mockMvc.perform(post("/app/public/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseCode.REFRESH_TOKEN_DATA_INVALID.toString()));
    }

    @Test
    void refreshTokenMobileNotMatch() throws Exception {

        String expiredToken = testData.generateExpiredToken(new HashMap<>(),
                StringUtil.formFullMobileNumberStr(USER_1_FULL_MOBILE_NUMBER));

        RefreshCredentialsDto refreshTokenDto = new RefreshCredentialsDto();
        /* Токен от USER_1, а моб. номер ставим USER_2*/
        refreshTokenDto.setFullNumber(USER_2_FULL_MOBILE_NUMBER);
        refreshTokenDto.setAuthToken(expiredToken);
        refreshTokenDto.setRefreshToken(REFRESH_TOKEN);

        mockMvc.perform(post("/app/public/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseCode.REFRESH_TOKEN_DATA_INVALID.toString()));
    }

    @Test
    void refreshTokenNotExpired() throws Exception {

        String token = testData.generateUser1AuthToken();

        RefreshCredentialsDto refreshTokenDto = new RefreshCredentialsDto();
        refreshTokenDto.setFullNumber(USER_1_FULL_MOBILE_NUMBER);
        refreshTokenDto.setAuthToken(token);
        refreshTokenDto.setRefreshToken(REFRESH_TOKEN);

        mockMvc.perform(post("/app/public/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.authToken").value(token))
                .andExpect(jsonPath("$.body.refreshToken").value(REFRESH_TOKEN));
    }

    @Test
    void refreshTokenNoFullNumber() throws Exception {

        String expiredToken = testData.generateExpiredToken(new HashMap<>(),
                StringUtil.formFullMobileNumberStr(USER_1_FULL_MOBILE_NUMBER));

        RefreshCredentialsDto refreshTokenDto = new RefreshCredentialsDto();
        refreshTokenDto.setFullNumber(null);
        refreshTokenDto.setAuthToken(expiredToken);
        refreshTokenDto.setRefreshToken(REFRESH_TOKEN);

        mockMvc.perform(post("/app/public/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createFirebaseToken() throws Exception {

        mockMvc.perform(post("/app/user/firebase?token=someTokenValue")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private SmsAuth getTestSmsAuth() {
        return smsAuthRepository.findByCountryCodeAndMobileNumber(AUTH_NEW_USER_FULL_MOBILE_NUMBER.getCountryCode(),
               AUTH_NEW_USER_FULL_MOBILE_NUMBER.getMobileNumber()).orElseThrow();
    }

}
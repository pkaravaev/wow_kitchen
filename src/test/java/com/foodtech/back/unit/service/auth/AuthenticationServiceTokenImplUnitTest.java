package com.foodtech.back.unit.service.auth;

import com.foodtech.back.dto.auth.AuthDto;
import com.foodtech.back.dto.auth.CredentialsAuthDto;
import com.foodtech.back.dto.auth.CredentialsDto;
import com.foodtech.back.dto.auth.RefreshCredentialsDto;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.security.JwtTokenService;
import com.foodtech.back.service.auth.AuthenticationServiceTokenImpl;
import com.foodtech.back.service.auth.SmsCodeCredentialsChecker;
import com.foodtech.back.service.auth.SmsCodeCredentialsSender;
import com.foodtech.back.service.model.UserServiceImpl;
import com.foodtech.back.util.exceptions.TokenRefreshDataInvalidException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultHeader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("unit")
@TestPropertySource(value = {
        "classpath:sms.properties",
        "classpath:crypt.properties",
        "classpath:iiko.properties",
        "classpath:delivery.properties",
        "classpath:push.properties",
        "classpath:payment.properties",
        "classpath:client_messages.properties",
        "classpath:rabbitmq.properties",

        "classpath:payment-dev.properties"
})
class AuthenticationServiceTokenImplUnitTest {

    private static final String COUNTRY_CODE = "7";
    private static final String MOBILE_NUMBER = "9999999999";
    private static final FullMobileNumber FULL_MOBILE_NUMBER = new FullMobileNumber(COUNTRY_CODE, MOBILE_NUMBER);
    private static final String AUTH_TOKEN = "NEW_AUTH_TOKEN";
    private static final String REFRESH_TOKEN = "NEW_REFRESH_TOKEN";

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setCountryCode(COUNTRY_CODE);
        user.setMobileNumber(MOBILE_NUMBER);
        return user;
    }

    @Autowired
    AuthenticationServiceTokenImpl authService;

    @MockBean
    SmsCodeCredentialsSender codeSender;
    
    @MockBean
    SmsCodeCredentialsChecker codeChecker;

    @MockBean
    UserServiceImpl userService;

    @MockBean
    JwtTokenService tokenService;

    @Test
    void sendAuthCredentials() {
        AuthDto authDto = new AuthDto();
        authService.sendAuthCredentials(authDto);
        verify(codeSender, times(1)).sendCredentials(authDto);
    }

    @Test
    void authenticateNewUser() {

        //when
        CredentialsAuthDto credentialsAuthDto = new CredentialsAuthDto();
        when(userService.login(credentialsAuthDto)).thenReturn(user());
        when(tokenService.getAuthToken(anyString())).thenReturn(AUTH_TOKEN);
        when(tokenService.getRefreshToken(user())).thenReturn(REFRESH_TOKEN);

        //do
        CredentialsDto actual = authService.authenticate(credentialsAuthDto);

        //then
        verify(codeChecker, times(1)).checkCredentials(credentialsAuthDto);
        CredentialsDto expected = new CredentialsDto();
        expected.setAuthToken(AUTH_TOKEN);
        expected.setRefreshToken(REFRESH_TOKEN);
        assertEquals(expected, actual);
    }

    @Test
    void refreshCredentials() {
        RefreshCredentialsDto refreshDto = new RefreshCredentialsDto();
        refreshDto.setFullNumber(FULL_MOBILE_NUMBER);
        refreshDto.setAuthToken("OLD_AUTH_TOKEN");
        refreshDto.setRefreshToken(REFRESH_TOKEN);

        DefaultClaims claims = new DefaultClaims();
        claims.setSubject(FULL_MOBILE_NUMBER.getMobileNumber());

        when(userService.get(FULL_MOBILE_NUMBER)).thenReturn(Optional.of(user()));
        when(tokenService.getFullMobileFromToken(refreshDto.getAuthToken())).thenThrow(new ExpiredJwtException(new DefaultHeader(), claims, ""));
        when(tokenService.getAuthToken(user().getFullMobileNumberStr())).thenReturn(AUTH_TOKEN);
        when(tokenService.getRefreshToken(user())).thenReturn(refreshDto.getRefreshToken());

        CredentialsDto actual = authService.refreshCredentials(refreshDto);

        assertEquals(AUTH_TOKEN, actual.getAuthToken());
        assertEquals(refreshDto.getRefreshToken(), actual.getRefreshToken());
    }

    @Test
    void refreshCredentialsMalformedException() {

        //when
        when(userService.get(FULL_MOBILE_NUMBER)).thenReturn(Optional.of(user()));
        RefreshCredentialsDto refreshDto = new RefreshCredentialsDto();
        refreshDto.setFullNumber(FULL_MOBILE_NUMBER);
        refreshDto.setAuthToken("OLD_AUTH_TOKEN");
        refreshDto.setRefreshToken(REFRESH_TOKEN);
        when(tokenService.getFullMobileFromToken(refreshDto.getAuthToken())).thenThrow(new MalformedJwtException(""));
        when(tokenService.getAuthToken(user().getFullMobileNumberStr())).thenReturn(AUTH_TOKEN);
        when(tokenService.getRefreshToken(user())).thenReturn(refreshDto.getRefreshToken());

        //then
        assertThrows(TokenRefreshDataInvalidException.class, () -> authService.refreshCredentials(refreshDto));
    }

    @Test
    void refreshCredentialsNotExpired() {

        //when
        when(userService.get(FULL_MOBILE_NUMBER)).thenReturn(Optional.of(user()));
        RefreshCredentialsDto refreshDto = new RefreshCredentialsDto();
        refreshDto.setFullNumber(FULL_MOBILE_NUMBER);
        refreshDto.setAuthToken("OLD_AUTH_TOKEN");
        refreshDto.setRefreshToken(REFRESH_TOKEN);
        when(tokenService.getAuthToken(user().getFullMobileNumberStr())).thenReturn(AUTH_TOKEN);
        when(tokenService.getRefreshToken(user())).thenReturn(refreshDto.getRefreshToken());

        //do
        CredentialsDto actual = authService.refreshCredentials(refreshDto);

        assertEquals(refreshDto.getAuthToken(), actual.getAuthToken());
        assertEquals(refreshDto.getRefreshToken(), actual.getRefreshToken());
    }
}
package com.foodtech.back.unit.service.security;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.entity.auth.RefreshToken;
import com.foodtech.back.entity.bonus.BonusAccount;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.util.AppProperty;
import com.foodtech.back.repository.auth.RefreshTokenRepository;
import com.foodtech.back.repository.util.PropertiesRepository;
import com.foodtech.back.security.JwtTokenService;
import com.foodtech.back.security.JwtUser;
import com.foodtech.back.service.auth.BlacklistAuthTokenService;
import com.foodtech.back.service.properties.PropertiesService;
import com.foodtech.back.util.DateUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static com.foodtech.back.security.JwtTokenService.DEFAULT_EXPIRATION_AFTER_MILLIS;
import static com.foodtech.back.util.DateUtil.toDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
class JwtTokenServiceUnitTest {

    private static final String COUNTRY_CODE = "7";
    private static final String MOBILE_NUMBER = "9999999999";

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setCountryCode(COUNTRY_CODE);
        user.setMobileNumber(MOBILE_NUMBER);
        return user;
    }

    private static JwtUser jwtUser() {
        return new JwtUser(user().getId(), user().getMobileNumber(), user().getCountryCode(),
                user().getFullMobileNumberStr(), Collections.emptySet(), new BonusAccount(),
                LocalDateTime.of(2019, 1, 1, 0, 0), true);
    }

    @Autowired
    JwtTokenService tokenService;

    @Autowired
    ResourcesProperties properties;

    @MockBean
    PropertiesRepository propertiesRepository;

    @MockBean
    BlacklistAuthTokenService blacklistTokenService;

    @MockBean
    RefreshTokenRepository refreshTokenRepository;

    @Test
    void getAuthTokenDefaultExpiration() {

        //do
        String authToken = tokenService.getAuthToken(user().getFullMobileNumberStr());

        //then
        assertAuthTokenValid(authToken);
        assertExpirationTimeDefault(authToken);
    }

    @Test
    void getAuthTokenExpirationGetException() {
        //when
        AppProperty appProperty = new AppProperty();
        appProperty.setName(PropertiesService.TOKEN_EXPIRED_MIN);
        long min = DEFAULT_EXPIRATION_AFTER_MILLIS + 86_400_000; // дефолтное + 1 сутки
        min = min/1000/60;
        appProperty.setValue(String.valueOf(min));
        when(propertiesRepository.findByNameEquals(PropertiesService.TOKEN_EXPIRED_MIN)).thenThrow(new CannotGetJdbcConnectionException(""));

        //do
        String authToken = tokenService.getAuthToken(user().getFullMobileNumberStr());

        //then
        assertAuthTokenValid(authToken);
        assertExpirationTimeDefault(authToken);
    }

    private void assertExpirationTimeDefault(String authToken) {
        LocalDateTime expirationDate = DateUtil.toLocalDateTime(tokenService.getExpirationDateFromToken(authToken));
        assertTrue(LocalDateTime.now().plusSeconds(DEFAULT_EXPIRATION_AFTER_MILLIS/1000).isAfter(expirationDate));
    }

    @Test
    void getAuthTokenExpirationFromDb() {
        //when
        AppProperty appProperty = new AppProperty();
        appProperty.setName(PropertiesService.TOKEN_EXPIRED_MIN);
        long min = DEFAULT_EXPIRATION_AFTER_MILLIS + 86_400_000; // дефолтное + 1 сутки
        min = min/1000/60;
        appProperty.setValue(String.valueOf(min));
        when(propertiesRepository.findByNameEquals(PropertiesService.TOKEN_EXPIRED_MIN)).thenReturn(Optional.of(appProperty));

        //do
        String authToken = tokenService.getAuthToken(user().getFullMobileNumberStr());

        //then
        assertAuthTokenValid(authToken);
        assertExpirationTimeObtainedFromDb(authToken);
    }

    private void assertAuthTokenValid(String authToken) {
        assertTrue(StringUtils.hasText(authToken));
        String fullMobileFromToken = tokenService.getFullMobileFromToken(authToken);
        assertEquals(user().getFullMobileNumberStr(), fullMobileFromToken);
    }

    private void assertExpirationTimeObtainedFromDb(String authToken) {
        LocalDateTime expirationDate = DateUtil.toLocalDateTime(tokenService.getExpirationDateFromToken(authToken));
        assertFalse(LocalDateTime.now().plusSeconds(DEFAULT_EXPIRATION_AFTER_MILLIS/1000).isAfter(expirationDate));
    }

    @Test
    void getRefreshTokenExisted() {
        //when
        RefreshToken testToken = new RefreshToken();
        testToken.setToken("REFRESH_TOKEN");
        when(refreshTokenRepository.findByUserId(user().getId())).thenReturn(Optional.of(testToken));

        //do
        String refreshToken = tokenService.getRefreshToken(user());

        //then
        assertEquals(testToken.getToken(), refreshToken);
    }

    @Test
    void getRefreshTokenCreateNew() {
        //do
        String refreshToken = tokenService.getRefreshToken(user());

        //then
        assertTrue(StringUtils.hasText(refreshToken));
    }

    @Test
    void getMobileFromInvalidToken() {
        assertThrows(MalformedJwtException.class, () -> tokenService.getFullMobileFromToken("INVALID_TOKEN"));
    }

    @Test
    void getMobileFromExpiredToken() {
        assertThrows(ExpiredJwtException.class, () -> tokenService.getFullMobileFromToken(generateExpiredToken()));
    }

    private String generateExpiredToken() {
        final LocalDateTime createdDate = LocalDateTime.now();

        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(user().getFullMobileNumberStr())
                .setIssuedAt(toDate(createdDate))
                .setExpiration(toDate(createdDate))
                .signWith(SignatureAlgorithm.HS512, properties.getSecret())
                .compact();
    }

    @Test
    void tokenIsValid() {
        String token = tokenService.getAuthToken(user().getFullMobileNumberStr());
        assertTrue(tokenService.tokenIsValidForUser(token, jwtUser()));
    }

    @Test
    void tokenInvalid() {
        String token = "INVALID_TOKEN";
        assertThrows(MalformedJwtException.class, () -> tokenService.tokenIsValidForUser(token, jwtUser()));
    }

    @Test
    void tokenInvalidMobileNotEqual() {
        String token = tokenService.getAuthToken("7-7777777777");
        assertFalse(tokenService.tokenIsValidForUser(token, jwtUser()));
    }

    @Test
    void tokenInBlacklist() {
        //when
        String token = tokenService.getAuthToken(user().getFullMobileNumberStr());
        when(blacklistTokenService.tokenInBlacklist(token)).thenReturn(true);
        //do
        assertFalse(tokenService.tokenIsValidForUser(token, jwtUser()));
    }

    @Test
    void tokenBeforeUserHardLogout() {
        //when
        String token = tokenService.getAuthToken(user().getFullMobileNumberStr());
        LocalDateTime hardLogoutDate = LocalDateTime.now();
        JwtUser jwtUser = new JwtUser(user().getId(), user().getMobileNumber(), user().getCountryCode(),
                user().getFullMobileNumberStr(), Collections.emptySet(), new BonusAccount(), hardLogoutDate, true);
        //do
        assertFalse(tokenService.tokenIsValidForUser(token, jwtUser));
    }

}
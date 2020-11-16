package com.foodtech.back.security;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.entity.auth.RefreshToken;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.util.AppProperty;
import com.foodtech.back.repository.auth.RefreshTokenRepository;
import com.foodtech.back.repository.util.PropertiesRepository;
import com.foodtech.back.service.auth.BlacklistAuthTokenService;
import com.foodtech.back.service.properties.PropertiesService;
import com.foodtech.back.util.exceptions.TokenRefreshDataInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.foodtech.back.util.DateUtil.*;
import static com.foodtech.back.util.StringUtil.formFullMobileNumberStr;
import static java.util.Objects.isNull;

@Service
@Slf4j
public class JwtTokenService {

    public static final Long DEFAULT_EXPIRATION_AFTER_MILLIS = 604_800_000L; // 7 days

    private final ResourcesProperties properties;

    private final PropertiesRepository propertiesRepository;

    private final BlacklistAuthTokenService blacklistTokenService;

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtTokenService(ResourcesProperties properties, PropertiesRepository propertiesRepository,
                           BlacklistAuthTokenService blacklistTokenService, RefreshTokenRepository refreshTokenRepository) {
        this.properties = properties;
        this.propertiesRepository = propertiesRepository;
        this.blacklistTokenService = blacklistTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String getAuthToken(String subject) {
        Map<String, Object> claims = new HashMap<>();

        // subject - идентификатор юзера, в нашем случае строка countryCode + / + mobileNumber
        return generateAuthToken(claims, subject);
    }

    private String generateAuthToken(Map<String, Object> claims, String subject) {
        final LocalDateTime createdDate = LocalDateTime.now();

        final LocalDateTime expirationDate = calculateExpirationDate(createdDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(toDate(createdDate))
                .setExpiration(toDate(expirationDate))
                .signWith(SignatureAlgorithm.HS512, properties.getSecret())
                .compact();
    }

    private LocalDateTime calculateExpirationDate(LocalDateTime createdDate) {
        long expirationAfter = DEFAULT_EXPIRATION_AFTER_MILLIS;
        try {
            Optional<AppProperty> expiredMillis = propertiesRepository.findByNameEquals(PropertiesService.TOKEN_EXPIRED_MIN);
            if (expiredMillis.isPresent()) {
                expirationAfter = minutesToMillis(Integer.parseInt(expiredMillis.get().getValue()));
            }
        } catch (Exception ex) { log.info("Token expiration property retrieving failed - cause:{}", ex.getMessage()); }

        Instant instant = createdDate.atZone(ZONE_ID_DEFAULT).toInstant();
        long expirationMillis = instant.toEpochMilli() + expirationAfter;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(expirationMillis), ZONE_ID_DEFAULT);
    }

    private long minutesToMillis(int minutes) {
        return minutes * 60_000;
    }

    public String getRefreshToken(User user) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByUserId(user.getId());
        return tokenOpt.isPresent() ? tokenOpt.get().getToken() : createNewRefreshToken(user);
    }

    private String createNewRefreshToken(User user) {
        String token = generateRefreshToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private String generateRefreshToken() {
        return DigestUtils.sha256Hex(System.currentTimeMillis() + properties.getSecret());
    }

    public String getFullMobileFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(properties.getSecret())
                .parseClaimsJws(removeCtrlChars(token))
                .getBody();
    }

    private String removeCtrlChars(String token) {
        // С фронта периодически приходит token с невалидными whitespace в json'e
        //https://stackoverflow.com/questions/6198986/how-can-i-replace-non-printable-unicode-characters-in-java
        return token.replaceAll("\\p{Cntrl}", "")
                .replaceAll("[^\\p{Print}]", "")
                .replaceAll("\\p{C}", "");
    }

    public boolean tokenIsValidForUser(String token, JwtUser jwtUser) {

        // Если токен в "черном списке", отказываем в доступе
        if (blacklistTokenService.tokenInBlacklist(token)) {
            return false;
        }

        final String fullMobileStr = getFullMobileFromToken(token);
        boolean mobileEquals = fullMobileStr.equals(jwtUser.getUsername());
        if (!mobileEquals) {
            return false;
        }

        // Поле hardLogoutLastTime юзера дает возможность установить время, когда был совершен логаут
        // и все токены, выданные до этого времени считаются невалидными
        LocalDateTime issued = toLocalDateTime(getIssuedAtDateFromToken(token));
        LocalDateTime hardLogoutLastTime = jwtUser.getHardLogoutLastTime();
        return issuedAfterLogout(issued, hardLogoutLastTime);
    }

    private Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    private boolean issuedAfterLogout(LocalDateTime issued, LocalDateTime hardLogoutLastTime) {
        return isNull(hardLogoutLastTime) || hardLogoutLastTime.isBefore(issued);
    }

    public void checkRefreshTokenDataIsValid(String mobileFromToken, FullMobileNumber mobileFromFront, String token,
                                              String refreshToken, Long userId) {
        boolean isValid = true;

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByUserId(userId);
        if (tokenOpt.isEmpty() || !tokenOpt.get().getToken().equals(refreshToken)) {
            log.warn("Refreshing token for '{}' failed - refresh token doesn't match", mobileFromFront);
            isValid = false;

        } else if (blacklistTokenService.tokenInBlacklist(token)) {
            log.warn("Refreshing token for '{}' failed - token in blacklist", mobileFromFront);
            isValid = false;

        } else if (!mobileFromToken.equals(formFullMobileNumberStr(mobileFromFront))) {
            log.warn("Refreshing token for '{}' failed - mobile number doesn't match token data", mobileFromFront);
            isValid = false;
        }

        if (!isValid) {
            throw new TokenRefreshDataInvalidException(mobileFromFront.toString());
        }
    }

}

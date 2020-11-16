package com.foodtech.back.service.auth;

import com.foodtech.back.dto.auth.AuthDto;
import com.foodtech.back.dto.auth.CredentialsAuthDto;
import com.foodtech.back.dto.auth.CredentialsDto;
import com.foodtech.back.dto.auth.RefreshCredentialsDto;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.security.JwtTokenService;
import com.foodtech.back.service.model.UserService;
import com.foodtech.back.util.exceptions.TokenRefreshDataInvalidException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationServiceTokenImpl implements AuthenticationService {

    private final AuthCredentialsSender credentialsSender;

    private final AuthCredentialsChecker codeChecker;

    private final UserService userService;

    private final JwtTokenService tokenService;

    public AuthenticationServiceTokenImpl(AuthCredentialsSender credentialsSender,
                                          AuthCredentialsChecker codeChecker,
                                          UserService userService,
                                          JwtTokenService tokenService) {
        this.credentialsSender = credentialsSender;
        this.codeChecker = codeChecker;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Override
    public void sendAuthCredentials(AuthDto authDto) {
        credentialsSender.sendCredentials(authDto);
    }

    @Override
    public CredentialsDto authenticate(CredentialsAuthDto authDto) {
        codeChecker.checkCredentials(authDto);
        User user = userService.login(authDto);
        return formTokens(user);
    }

    private CredentialsDto formTokens(User user) {
        String authToken = tokenService.getAuthToken(user.getFullMobileNumberStr());
        String refreshToken = tokenService.getRefreshToken(user);
        CredentialsDto credentialsDto = new CredentialsDto();
        credentialsDto.setAuthToken(authToken);
        credentialsDto.setRefreshToken(refreshToken);
        return credentialsDto;
    }

    @Override
    public CredentialsDto refreshCredentials(RefreshCredentialsDto credentialsDto) {
        FullMobileNumber fullNumber = credentialsDto.getFullNumber();
        String authToken = credentialsDto.getAuthToken();
        String refreshToken = credentialsDto.getRefreshToken();
        User user = userService.get(fullNumber).orElseThrow();

        try {
            tokenService.getFullMobileFromToken(authToken);

            /* https://github.com/jwtk/jjwt/issues/183
            * У истекшего токена получить subject можно только через обработку исключения */
        } catch (ExpiredJwtException ex) {
            tokenService.checkRefreshTokenDataIsValid(ex.getClaims().getSubject(), fullNumber, authToken, refreshToken, user.getId());
            authToken = tokenService.getAuthToken(user.getFullMobileNumberStr());
            refreshToken = tokenService.getRefreshToken(user);
            return new CredentialsDto(authToken, refreshToken);

        } catch (MalformedJwtException ex) {
            throw new TokenRefreshDataInvalidException(fullNumber.toString());
        }

        /* Если токен не истек, то просто возвращаем тот же токен обратно */
        return new CredentialsDto(authToken, refreshToken);
    }
}

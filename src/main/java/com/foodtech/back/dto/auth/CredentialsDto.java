package com.foodtech.back.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class CredentialsDto {

    //TODO после рефакторинга сервиса авторизации на абстрактные классы Credentials (на случай добавления способов авторизации),
    // надо бы вынести authToken и refreshToken в отдельный класс-наследник (к примеру TokenCredentials),
    // но для этого нужно сначала переделать модель на фронте.
    private String authToken;
    private String refreshToken;

    public CredentialsDto(String authToken, String refreshToken) {
        this.authToken = authToken;
        this.refreshToken = refreshToken;
    }
}

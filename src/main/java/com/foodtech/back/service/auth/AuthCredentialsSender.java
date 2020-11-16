package com.foodtech.back.service.auth;

import com.foodtech.back.dto.auth.AuthDto;

public interface AuthCredentialsSender {

    void sendCredentials(AuthDto authDto);
}

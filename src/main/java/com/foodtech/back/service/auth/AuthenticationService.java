package com.foodtech.back.service.auth;

import com.foodtech.back.dto.auth.AuthDto;
import com.foodtech.back.dto.auth.CredentialsDto;
import com.foodtech.back.dto.auth.RefreshCredentialsDto;
import com.foodtech.back.dto.auth.CredentialsAuthDto;

public interface AuthenticationService {

    void sendAuthCredentials(AuthDto authDto);

    CredentialsDto authenticate(CredentialsAuthDto authDto);

    CredentialsDto refreshCredentials(RefreshCredentialsDto credentialsDto);

}

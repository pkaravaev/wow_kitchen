package com.foodtech.back.service.auth;

import com.foodtech.back.dto.auth.CredentialsAuthDto;

public interface AuthCredentialsChecker {

    void checkCredentials(CredentialsAuthDto authDto);
}

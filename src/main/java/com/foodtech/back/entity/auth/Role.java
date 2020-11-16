package com.foodtech.back.entity.auth;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    ROLE_USER,
    ROLE_ADMIN,
    ROLE_MASTER;

    @Override
    public String getAuthority() {
        return name();
    }
}

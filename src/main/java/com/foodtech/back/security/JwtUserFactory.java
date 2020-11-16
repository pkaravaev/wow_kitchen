package com.foodtech.back.security;

import com.foodtech.back.entity.model.User;

public final class JwtUserFactory {

    private JwtUserFactory() { }

    public static JwtUser create(User user) {
        return new JwtUser(user.getId(), user.getMobileNumber(), user.getCountryCode(), user.getName(), user.getRoles(),
                user.getBonusAccount(), user.getHardLogoutLastTime(), user.getEnabled());
    }
}

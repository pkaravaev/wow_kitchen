package com.foodtech.back.security;

import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.service.model.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.foodtech.back.service.model.CacheService.LOGGED_USER_CACHE;
import static com.foodtech.back.util.StringUtil.splitFullMobileNumber;

@Service("jwtUserDetailsService")
public class JwtUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public JwtUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    // В соответствии с нашей бизнес-логикой юзер идентифицируется только по номеру телефона
    @Override
    @Cacheable(LOGGED_USER_CACHE)
    public UserDetails loadUserByUsername(String fullMobileNumberStr) throws UsernameNotFoundException {

        FullMobileNumber fullMobileNumber = splitFullMobileNumber(fullMobileNumberStr);
        User user = userService.get(fullMobileNumber).orElseThrow();
        return JwtUserFactory.create(user);
    }
}

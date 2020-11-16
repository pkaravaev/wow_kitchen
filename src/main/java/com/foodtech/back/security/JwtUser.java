package com.foodtech.back.security;

import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.entity.bonus.BonusAccount;
import com.foodtech.back.util.StringUtil;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
public class JwtUser implements UserDetails {

    private final Long id;
    private final String mobileNumber;
    private final String countryCode;
    private final String name;
    private final Collection<? extends GrantedAuthority> authorities;
    private BonusAccount bonusAccount;
    private LocalDateTime hardLogoutLastTime;
    private final boolean enabled;

    public JwtUser(Long id, String mobileNumber, String countryCode, String name,
                   Collection<? extends GrantedAuthority> authorities, BonusAccount bonusAccount,
                   LocalDateTime hardLogoutLastTime, boolean enabled) {
        this.id = id;
        this.mobileNumber = mobileNumber;
        this.countryCode = countryCode;
        this.name = name;
        this.authorities = authorities;
        this.bonusAccount = bonusAccount;
        this.hardLogoutLastTime = hardLogoutLastTime;
        this.enabled = enabled;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    // Идентфицируем юзера по сочетанию countryCode и mobileNumber
    @Override
    public String getUsername() {
        return StringUtil.formFullMobileNumberStr(countryCode, mobileNumber);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public FullMobileNumber getFullMobileNumber() {
        return new FullMobileNumber(countryCode, mobileNumber);
    }

    @Override
    public String toString() {
        return "+" + countryCode + mobileNumber;
    }
}

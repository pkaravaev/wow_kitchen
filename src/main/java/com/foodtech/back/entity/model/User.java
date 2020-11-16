package com.foodtech.back.entity.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.entity.AbstractIdEntity;
import com.foodtech.back.entity.auth.Role;
import com.foodtech.back.entity.bonus.BonusAccount;
import com.foodtech.back.entity.bonus.PromoCodeImpersonal;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.util.StringUtil;
import com.foodtech.back.util.converter.JpaCryptoConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tb_user", schema = "public")
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends AbstractIdEntity {

    private String name;

    @EqualsAndHashCode.Include
    private String countryCode;

    @Convert(converter = JpaCryptoConverter.class)
    @EqualsAndHashCode.Include
    private String mobileNumber;

    private Boolean enabled;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("created DESC")
    @JsonManagedReference
    private List<Address> addresses;

    @Transient
    private Address actualAddress;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("actual")
    @JsonManagedReference
    private List<BankCard> cards;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_promo_code_impersonal_used",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "promo_code_id"))
    private List<PromoCodeImpersonal> usedPromoCodes;

    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tb_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @ElementCollection(fetch = FetchType.LAZY)
    private Set<Role> roles;

    private BonusAccount bonusAccount;

    // Это поле участвует в проверке токена, если токен выдан до момент логаута, то токен счиатется недействительным
    // (соотв. если установить в базе значение вручную, можно искусственно вызвать обнуление ранее выданных токенов
    //  - на случай завладения токеном)
    private LocalDateTime hardLogoutLastTime;

    public String getFullMobileNumberStr() {
        return StringUtil.formFullMobileNumberStr(countryCode, mobileNumber);
    }

    public FullMobileNumber getFullMobileNumber() {
        return new FullMobileNumber(countryCode, mobileNumber);
    }

    @Override
    public String toString() {
        return "+" + countryCode + mobileNumber;
    }
}

package com.foodtech.back.entity.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foodtech.back.entity.AbstractIdEntity;
import com.foodtech.back.entity.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_user_bank_card")
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@ToString
public class BankCard extends AbstractIdEntity {

    @JsonIgnore
    @EqualsAndHashCode.Include
    private String token;

    @EqualsAndHashCode.Include
    private String cardMask;

    private String cardType;

    @JsonIgnore
    private String cardIssuer;

    private boolean actual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @EqualsAndHashCode.Include
    @ToString.Exclude
    private User user;
}

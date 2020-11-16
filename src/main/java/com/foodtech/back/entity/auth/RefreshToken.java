package com.foodtech.back.entity.auth;

import com.foodtech.back.entity.AbstractIdEntity;
import com.foodtech.back.entity.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_refresh_token")
@Data
@EqualsAndHashCode(callSuper = false)
public class RefreshToken extends AbstractIdEntity {

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    private String token;
}

package com.foodtech.back.entity.auth;

import com.foodtech.back.entity.AbstractIdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tb_auth_token_blacklist")
@Data
@EqualsAndHashCode(callSuper = false)
public class BlacklistAuthToken extends AbstractIdEntity {

    private String token;
}

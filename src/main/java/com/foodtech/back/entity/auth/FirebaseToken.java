package com.foodtech.back.entity.auth;

import com.foodtech.back.entity.AbstractIdEntity;
import com.foodtech.back.entity.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_firebase_token")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class FirebaseToken extends AbstractIdEntity {

    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public FirebaseToken(String token) {
        this.token = token;
    }

    public FirebaseToken(String token, User user) {
        this.token = token;
        this.user = user;
    }
}

package com.foodtech.back.entity.auth;

import com.foodtech.back.entity.AbstractIdEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "tb_admin")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"password", "roles"}, callSuper = false)
public class Admin extends AbstractIdEntity {

    private String name;
    private String password;

    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tb_admin_roles", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "role")
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
}

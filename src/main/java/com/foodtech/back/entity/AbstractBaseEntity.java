package com.foodtech.back.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public class AbstractBaseEntity {

    @JsonIgnore
    protected LocalDateTime created;

    @JsonIgnore
    protected LocalDateTime updated;

    @PrePersist
    void onCreate() {
        this.created = LocalDateTime.now();
    }

    @PreUpdate
    void onPersist() {
        this.updated = LocalDateTime.now();
    }

}

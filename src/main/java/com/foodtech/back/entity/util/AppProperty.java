package com.foodtech.back.entity.util;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/*
* Настройки приложения
* */

@Entity
@Table(name = "tb_properties")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AppProperty {

    @Id
    private Integer id;

    @EqualsAndHashCode.Include
    @Column(name = "property_name")
    private String name;

    @Column(name = "property_value")
    private String value;

    private String description;
}

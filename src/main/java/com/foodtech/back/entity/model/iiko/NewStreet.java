package com.foodtech.back.entity.model.iiko;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

// Из-за проблем с картографией в Казахстане, будем сохранять улицы, пришедшие с фронта и ненайденные в справочнике
// в базу, чтобы затем сохранить в справочнике iiko
@Entity
@Table(name = "tb_new_street")
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class NewStreet {

    @Id
    private String name;

    public NewStreet(String name) {
        this.name = name;
    }
}

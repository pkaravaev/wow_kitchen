package com.foodtech.back.entity.model.iiko;

import com.foodtech.back.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

// Сущность города из справочника городов/улиц, загруженного в iiko
@Entity
@Table(name = "tb_iiko_city_directory")
@Data
@EqualsAndHashCode(exclude = {"streets"}, callSuper = false)
public class IikoCityDirectory extends AbstractBaseEntity {

    @Id
    private String id;

    private String name;

    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<IikoStreetDirectory> streets;
}

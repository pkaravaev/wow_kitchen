package com.foodtech.back.entity.model.iiko;

import com.foodtech.back.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Table(name = "tb_iiko_street_directory")
@Data
@EqualsAndHashCode(callSuper = false)
public class IikoStreetDirectory extends AbstractBaseEntity {

    @Id
    private String id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private IikoCityDirectory city;
}

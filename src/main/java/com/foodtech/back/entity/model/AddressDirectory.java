package com.foodtech.back.entity.model;

import com.foodtech.back.entity.AbstractIdEntity;
import com.foodtech.back.entity.model.iiko.DeliveryZone;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

/* Элемент вручную составленного справочника адресов, входящих в зону доставки */
@Entity
@Table(name = "tb_address_directory")
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class AddressDirectory extends AbstractIdEntity {

    @EqualsAndHashCode.Include
    private String street;

    @EqualsAndHashCode.Include
    private String house;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @ManyToOne
    private DeliveryZone deliveryZone;

    public static AddressDirectory of(String street, String house) {
        AddressDirectory directory = new AddressDirectory();
        directory.setStreet(street);
        directory.setHouse(house);
        return directory;
    }
}

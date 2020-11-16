package com.foodtech.back.entity.model;

import com.foodtech.back.entity.AbstractIdEntity;
import com.foodtech.back.entity.model.iiko.DeliveryZone;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "tb_kitchen_delivery_terminal")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class KitchenDeliveryTerminal extends AbstractIdEntity {

    private String terminalName;

    private String terminalId;

    private String timeZone;

    @ManyToOne(fetch = FetchType.LAZY)
    private Kitchen kitchen;

    @OneToMany(mappedBy = "deliveryTerminal", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<DeliveryZone> deliveryZones;

    @OneToMany(mappedBy = "deliveryTerminal", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Address> addresses;

    @OneToMany(mappedBy = "deliveryTerminal", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<OpeningHours> openingHours;
}

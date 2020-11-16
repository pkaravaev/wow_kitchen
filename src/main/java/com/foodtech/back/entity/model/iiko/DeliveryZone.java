package com.foodtech.back.entity.model.iiko;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foodtech.back.entity.AbstractIdEntity;
import com.foodtech.back.entity.model.KitchenDeliveryTerminal;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "tb_delivery_zone")
@Data
@EqualsAndHashCode(exclude = {"coordinates"}, callSuper = false)
public class DeliveryZone extends AbstractIdEntity {

    private String name;

    private boolean active;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "tb_delivery_coordinate", joinColumns = @JoinColumn(name = "delivery_zone_id"))
    private List<DeliveryCoordinate> coordinates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private KitchenDeliveryTerminal deliveryTerminal;
}

package com.foodtech.back.entity.model;

import com.foodtech.back.entity.AbstractIdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "tb_kitchen")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class Kitchen extends AbstractIdEntity {

    private String organizationId;

    private String restaurantName;

    private String restaurantAddress;

    private String paymentTypeId;

    private String paymentTypeName;

    private String paymentTypeCode;

    private Long paymentTypeExternalRevision;

    private boolean paymentTypeCombinable;

    private boolean paymentTypeDeleted;

    @ToString.Exclude
    @OneToMany(mappedBy = "kitchen", fetch = FetchType.LAZY)
    private List<KitchenDeliveryTerminal> deliveryTerminals;
}

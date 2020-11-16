package com.foodtech.back.entity.model.iiko;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.foodtech.back.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "tb_product_category")
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Data
public class ProductCategory extends AbstractBaseEntity {

    @Id
    @EqualsAndHashCode.Include
    private String Id;

    private String name;

    private Integer viewOrder;

    @OneToMany(mappedBy = "productCategory", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonManagedReference
    private List<Product> products;

}

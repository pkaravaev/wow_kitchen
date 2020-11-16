package com.foodtech.back.entity.bonus;

import com.foodtech.back.entity.AbstractIdEntity;
import com.foodtech.back.entity.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "tb_promo_code_impersonal")
@Data
@EqualsAndHashCode(callSuper = false)
public class PromoCodeImpersonal extends AbstractIdEntity {

    private String promoCode;

    private Integer amount;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "usedPromoCodes")
    private List<User> usedByUsers;

}

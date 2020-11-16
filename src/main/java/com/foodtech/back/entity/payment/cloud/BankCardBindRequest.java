package com.foodtech.back.entity.payment.cloud;

import com.foodtech.back.entity.AbstractIdEntity;
import com.foodtech.back.entity.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_user_bank_card_bind_request")
@Data
@EqualsAndHashCode(callSuper = false)
public class BankCardBindRequest extends AbstractIdEntity {

    private Long transactionId;

    private String queueName;

    private String result;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}

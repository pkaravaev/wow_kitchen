package com.foodtech.back.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.foodtech.back.entity.AbstractIdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Entity
@Table(name = "tb_user_address")
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@JsonInclude(NON_NULL)
public class Address extends AbstractIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private User user;

    // Вся валидация в классе соответствует требованиям iiko api к адресу заказа
    @Size(min = 1, max = 255)
    @EqualsAndHashCode.Include
    private String city;

    @NotEmpty
    @Size(min = 1, max = 255)
    @EqualsAndHashCode.Include
    private String street;

    @NotEmpty
    @Size(min = 1, max = 20)
    @EqualsAndHashCode.Include
    private String home;

    @Size(min = 1, max = 10)
    @EqualsAndHashCode.Include
    private String housing; // Корпус

    @Size(min = 1, max = 10)
    @EqualsAndHashCode.Include
    private String apartment; // Квартира

    @Size(min = 1, max = 10)
    private String entrance; // Подъезд

    @Size(min = 1, max = 10)
    private String floor;

    @Size(min = 1, max = 10)
    private String doorphone;

    @Column(name = "address_comment")
    @Size(min = 1, max = 500)
    private String comment;

    private boolean actual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private KitchenDeliveryTerminal deliveryTerminal;

    public Address(String city, String street, String home) {
        this.city = city;
        this.street = street;
        this.home = home;
    }
}

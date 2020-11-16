package com.foodtech.back.dto.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.foodtech.back.entity.model.OrderStatus;
import com.foodtech.back.entity.model.Address;
import com.foodtech.back.entity.payment.BankCard;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderInfoDto {

    private Long id;

    private Integer totalCost;

    private List<OrderItemDto> orderItems;

    private String amqpHost;

    private String statusQueueName;

    private Integer appliedBonusAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime created;

    private OrderStatus orderStatus;

    private Address address;

    private String paymentType;

    private BankCard bankCard;

    private Integer totalDeliveryTime;

    private Long passedDeliveryTime;

    public OrderInfoDto(Long id, String amqpHost, String statusQueueName, Integer totalDeliveryTime) {
        this.id = id;
        this.amqpHost = amqpHost;
        this.statusQueueName = statusQueueName;
        this.totalDeliveryTime = totalDeliveryTime;
    }
}

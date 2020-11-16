package com.foodtech.back.dto.model;

import com.foodtech.back.entity.model.iiko.Product;
import lombok.Data;

@Data
public class OrderItemDto {

    private Product product;

    private int amount;
}

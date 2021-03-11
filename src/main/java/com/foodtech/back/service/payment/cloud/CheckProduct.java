package com.foodtech.back.service.payment.cloud;

import lombok.Data;

@Data
public class CheckProduct {
    private String label;
    private double price;
    private double quantity;
    private double amount;
    private String measurementUnit;
}

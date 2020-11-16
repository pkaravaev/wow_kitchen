package com.foodtech.back.dto.iiko;

import lombok.Data;

@Data
public class IikoOrderInfo {

    String orderId;
    String status;
    String number;
    DeliveryProblemInfo problem;
}

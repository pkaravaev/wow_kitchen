package com.foodtech.back.dto.payment.check;

import lombok.Data;

import java.util.List;

@Data
public class ReceiptCheck {
    private List<ItemCheck> Items;
}

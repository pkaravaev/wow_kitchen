package com.foodtech.back.service.payment.cloud;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CloudCheck {

    private List<CheckProduct> Items = new ArrayList<>();
}

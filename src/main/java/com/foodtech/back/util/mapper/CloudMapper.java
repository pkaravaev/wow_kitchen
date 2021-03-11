package com.foodtech.back.util.mapper;

import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.OrderItem;
import com.foodtech.back.service.payment.cloud.CheckProduct;
import com.foodtech.back.service.payment.cloud.CloudCheck;

import java.util.List;
import java.util.stream.Collectors;

public class CloudMapper {

    public static CloudCheck toCloudCheck(Order order) {
        CloudCheck cloudCheck = new CloudCheck();
        List<CheckProduct> checkProducts = order.getItems().stream()
                .map(CloudMapper::toCloudCheckProduct)
                .collect(Collectors.toList());
        cloudCheck.setItems(checkProducts);
        return cloudCheck;
    }

    private static CheckProduct toCloudCheckProduct(OrderItem orderItem) {
        CheckProduct result = new CheckProduct();
        result.setLabel(orderItem.getProduct().getName());
        result.setPrice(orderItem.getProduct().getPrice());
        result.setMeasurementUnit(orderItem.getProduct().getMeasureUnit());
        result.setQuantity(orderItem.getAmount());
        result.setAmount(orderItem.getAmount());
        return result;
    }
}

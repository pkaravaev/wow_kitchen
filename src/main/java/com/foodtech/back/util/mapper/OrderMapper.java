package com.foodtech.back.util.mapper;

import com.foodtech.back.dto.iiko.IikoOrderItem;
import com.foodtech.back.dto.model.OrderInfoDto;
import com.foodtech.back.dto.model.OrderItemDto;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.OrderItem;
import com.foodtech.back.entity.model.iiko.Product;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class OrderMapper {

    private OrderMapper() {
    }

    /**
     * DTO to Entity
     */

    public static List<OrderItem> toOrderItems(Order order, Map<Product, Integer> productsQuantity) {
        return productsQuantity.entrySet()
                .stream()
                .map(entry -> toOrderItem(entry.getKey(), entry.getValue(), order))
                .collect(Collectors.toList());
    }

    private static OrderItem toOrderItem(Product product, int amount, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setAmount(amount);
        return orderItem;
    }

    /**
     * Entity to DTO
     */

    public static List<OrderInfoDto> toOrderDtoList(List<Order> entityList) {
        return entityList
                .stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    public static OrderInfoDto toDto(Order entity) {
        OrderInfoDto dto = new OrderInfoDto();
        dto.setId(entity.getId());
        dto.setTotalCost(entity.getTotalCost());
        dto.setOrderStatus(entity.getStatus());
        dto.setOrderItems(toDtoList(entity.getItems()));
        dto.setCreated(nonNull(entity.getPaidTime()) ? entity.getPaidTime() : entity.getCreated());
        dto.setAddress(entity.getAddress());
        dto.setAppliedBonusAmount(entity.getAppliedBonusAmount());
        dto.setBankCard(entity.getBankCard());

        if (isNull(entity.getPaymentType())) {
            return dto;
        }
        dto.setPaymentType(entity.getPaymentType().toString());
        return dto;
    }

    private static List<OrderItemDto> toDtoList(List<OrderItem> entityList) {
        return entityList
                .stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    public static OrderInfoDto toProcessingDto(Order entity, String amqpHost) {
        if (isNull(entity)) { return null; }
        OrderInfoDto dto = new OrderInfoDto();
        dto.setId(entity.getId());
        dto.setAmqpHost(amqpHost);
        dto.setStatusQueueName(entity.getStatusQueueName());
        dto.setTotalDeliveryTime(entity.getDeliveryTime());
        dto.setPassedDeliveryTime(Duration.between(entity.getPaidTime(), LocalDateTime.now()).toMinutes());
        return dto;
    }


    private static OrderItemDto toDto(OrderItem entity) {
        OrderItemDto dto = new OrderItemDto();
        dto.setAmount(entity.getAmount());
        dto.setProduct(entity.getProduct());
        return dto;
    }

    /*
    * Entity to Iiko Request Dto
    * */

    public static List<IikoOrderItem> toIikoOrderItems(List<OrderItem> entityList) {
        return entityList.stream().map(OrderMapper::toIikoOrderItem).collect(Collectors.toList());
    }

    private static IikoOrderItem toIikoOrderItem(OrderItem entity) {
        Product product = entity.getProduct();
        return new IikoOrderItem(product.getId(), product.getName(), product.getCode(), entity.getAmount(), product.getPrice());
    }
}

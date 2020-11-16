package com.foodtech.back.dto.model;

import com.foodtech.back.util.exceptions.CartInvalidException;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class OrderRegistrationDto {

    @NotNull
    @Min(1)
    private Integer totalCost;

    @Valid
    @NotEmpty
    private List<OrderRegistrationItemDto> orderItems;

    private boolean cutlery;

    /* Мапа ID продукта -> его кол-во в заказе,
       используется для проверки правильности корзины заказа */
    public Map<String, Integer> getItemsQuantity() {
        if (!containsOnlyUniqueProductId()) {
            throw new CartInvalidException("Cart items list must contain only unique products ID");
        }

        return getOrderItems()
                .stream()
                .collect(Collectors.toMap(OrderRegistrationItemDto::getProductId, OrderRegistrationItemDto::getAmount));
    }

    private boolean containsOnlyUniqueProductId() {
        return getOrderItems().stream().allMatch(new HashSet<>()::add);
    }

}

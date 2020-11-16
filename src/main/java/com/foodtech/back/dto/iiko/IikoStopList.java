package com.foodtech.back.dto.iiko;

import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class IikoStopList {

    private List<IikoStopListItem> items;

    public Set<String> getItemsIdList() {
        return items
                .stream()
                .map(IikoStopListItem::getProductId)
                .collect(Collectors.toSet());
    }
}

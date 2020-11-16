package com.foodtech.back.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ExternalIikoProductCategoryDto {

    private String id;

    private String name;

    private List<String> products;

    public ExternalIikoProductCategoryDto(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

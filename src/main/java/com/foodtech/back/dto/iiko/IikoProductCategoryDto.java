package com.foodtech.back.dto.iiko;

import lombok.Data;

import java.util.List;

@Data
public class IikoProductCategoryDto {

    private String id;

    private String name;

    private List<IikoProductDto> productDtoList;
}

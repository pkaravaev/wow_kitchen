package com.foodtech.back.dto.iiko;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class IikoNomenclatureDto {

    private Set<IikoProductDto> products;

    private Set<IikoProductCategoryDto> productCategories;

    private long revision;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime uploadDate;
}

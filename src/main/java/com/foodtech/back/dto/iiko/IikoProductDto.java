package com.foodtech.back.dto.iiko;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.foodtech.back.entity.model.iiko.ImageInfo;
import com.foodtech.back.entity.model.iiko.Modifer;
import com.foodtech.back.util.converter.RoundBigDecimalConverter;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class IikoProductDto {

    private String id; // iiko product ID

    private String name;

    private String code; // Артикул

    private String description;

    private int order; // Порядок отображения

    private String parentGroup; // ID родительской группы

    private List<ImageInfo> images;

    private String groupId;

    private String productCategoryId;

    private int price;

    @JsonDeserialize(converter = RoundBigDecimalConverter.class)
    private BigDecimal carbohydrateAmount; // Количество углеводов на 100г блюда

    @JsonDeserialize(converter = RoundBigDecimalConverter.class)
    private BigDecimal energyAmount; // Энергетическая ценность на 100г блюда

    @JsonDeserialize(converter = RoundBigDecimalConverter.class)
    private BigDecimal fatAmount; // Количество жиров на 100г блюда

    @JsonDeserialize(converter = RoundBigDecimalConverter.class)
    private BigDecimal fiberAmount; // Количество жиров на 100г блюда

    @JsonDeserialize(converter = RoundBigDecimalConverter.class)
    private BigDecimal carbohydrateFullAmount; // Количество углеводов в блюде

    @JsonDeserialize(converter = RoundBigDecimalConverter.class)
    private BigDecimal energyFullAmount; // Энергетическая ценность в блюде

    @JsonDeserialize(converter = RoundBigDecimalConverter.class)
    private BigDecimal fatFullAmount; // Количество жиров в блюде

    @JsonDeserialize(converter = RoundBigDecimalConverter.class)
    private BigDecimal fiberFullAmount; // Количество белков в блюде

    private BigDecimal weight; // Вес одной единицы в кг

    private String type; // dish, good, modifier

    private List<Modifer> modifiers;

    private List<Modifer> groupModifiers;

    private String additionalInfo;

    private List<String> tags; //todo в базу будем сохранять обычным стрингом через #

    private String measureUnit; // Единица измерения товара ( кг, л, шт, порц.)

    private boolean doNotPrintInCheque; // Блюдо не нужно печатать на чеке. Актуально только для модификаторов

    private List<String> prohibitedToSaleOn; // Список ID терминалов, на которых продукт запрещен к продаже

    private boolean useBalanceForSell; // Товар продается на вес

}

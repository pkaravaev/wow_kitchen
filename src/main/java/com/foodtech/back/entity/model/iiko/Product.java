package com.foodtech.back.entity.model.iiko;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foodtech.back.dto.model.ProductPreferences;
import com.foodtech.back.entity.AbstractBaseEntity;
import com.foodtech.back.util.converter.PreferencesKeyWordsConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "tb_product")
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Product extends AbstractBaseEntity {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    private ProductCategory productCategory;

    private String description;

    private int price;

    private BigDecimal carbohydrateAmount; // Количество углеводов на 100г блюда

    private BigDecimal energyAmount; // Энергетическая ценность на 100г блюда

    private BigDecimal fatAmount; // Количество жиров на 100г блюда

    private BigDecimal fiberAmount; // Количество жиров на 100г блюда

    private BigDecimal carbohydrateFullAmount; // Количество углеводов в блюде

    private BigDecimal energyFullAmount; // Энергетическая ценность в блюде

    private BigDecimal fatFullAmount; // Количество жиров в блюде

    private BigDecimal fiberFullAmount; // Количество белков в блюде

    private BigDecimal weight; // Вес одной единицы в кг

    private String type; // dish, good, modifier

    private String additionalInfo;

    private String tags; //todo в базу будем сохранять обычным стрингом через #

    private String measureUnit; // Единица измерения товара ( кг, л, шт, порц.)

    private String image;

    private boolean doNotPrintInCheque; // Блюдо не нужно печатать на чеке. Актуально только для модификаторов

    private boolean useBalanceForSell; // Товар продается на вес

    private String productCategoryName; // По просьбе фронта упрощаем отображение списка в приложении

    @Column(name = "\"order\"")
    @JsonIgnore
    private int order; // Порядок отображения

    @JsonIgnore
    private String code; // Артикул

    @JsonIgnore
    private String groupId;

    @JsonIgnore
    private boolean includedInMenu;

    @JsonIgnore
    private boolean inStopList;

    @JsonIgnore
    private boolean vegetarian;

    @JsonIgnore
    private boolean spicy;

    @JsonIgnore
    private boolean withNuts;

    @Convert(converter = PreferencesKeyWordsConverter.class)
    @JsonIgnore
    @SuppressWarnings("JpaAttributeTypeInspection")
    private Set<String> productPreferenceKeyWords;

    @Transient
    private ProductPreferences userPreferences;

//  private String parentGroup; // ID родительской группы (пока не ясно будет ли использоваться вообще)

//  private List<Modifer> groupModifiers; группы пока не используем

//  private List<Modifer> modifiers; //todo таблица и сущность модификаторов

}

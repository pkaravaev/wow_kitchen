package com.foodtech.back.util.mapper;

import com.foodtech.back.dto.iiko.IikoDeliveryCoordinateDto;
import com.foodtech.back.dto.iiko.IikoDeliveryZoneDto;
import com.foodtech.back.dto.iiko.IikoProductCategoryDto;
import com.foodtech.back.dto.iiko.IikoProductDto;
import com.foodtech.back.entity.model.iiko.*;
import org.modelmapper.ModelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IikoMappers {

    private static final ModelMapper MAPPER = new ModelMapper();

    private IikoMappers() {
    }

    /**
     * DTO to Entity
     */

    public static Set<ProductCategory> toCategoryEntities(Set<IikoProductCategoryDto> dtoSet) {
        return dtoSet
                .stream()
                .map(IikoMappers::toProductCategoryEntity)
                .collect(Collectors.toSet());
    }

    private static ProductCategory toProductCategoryEntity(IikoProductCategoryDto dto) {
        ProductCategory newCategory = new ProductCategory();
        newCategory.setId(dto.getId());
        setViewOrder(newCategory, dto.getName());
        return newCategory;
    }

    /* По соглашению, в айко названия категорий забиты в виде "НомерДляПорядкаОтображения#НазваниеКатегории" */
    private static void setViewOrder(ProductCategory newCategory, String dtoName) {
        int viewOrder = 0;

        String[] strings = dtoName.split("#");
        if (strings.length == 2 && strings[0].chars().allMatch(Character::isDigit)) {
            viewOrder = Integer.parseInt(strings[0]);
            dtoName = strings[1];
        }
        newCategory.setViewOrder(viewOrder);
        newCategory.setName(dtoName);
    }

    public static Set<Product> toProductEntities(Collection<IikoProductDto> productsFromIiko,
                                                 Collection<Product> productsFromDatabase, Collection<ProductCategory> categories) {

        /* category ID -> category name map*/
        Map<String, String> categoryNames = categories.stream()
                .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));

        /* product ID -> product map*/
        Map<String, Product> productsFromDatabaseMap = productsFromDatabase
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return productsFromIiko
                .stream()
                .map(p -> toProductEntity(p, categoryNames.get(p.getProductCategoryId()), productsFromDatabaseMap))
                .collect(Collectors.toSet());
    }

    private static Product toProductEntity(IikoProductDto dto, String categoryName, Map<String, Product> productsFromDatabaseMap) {
        Product newProduct = MAPPER.map(dto, Product.class);
        newProduct.setProductCategoryName(categoryName);
        newProduct.setIncludedInMenu(true);
        setPreferencesKeyWords(newProduct, dto.getDescription());
        updatePreferencesState(newProduct, productsFromDatabaseMap.get(dto.getId()));
        setImage(newProduct, dto.getImages());
        return newProduct;
    }

    private static void setPreferencesKeyWords(Product newProduct, String dtoDescription) {
        if (Objects.isNull(dtoDescription)) {
            return;
        }
        Set<String> keyWords = new HashSet<>(Arrays.asList(dtoDescription.split(",")));
        newProduct.setProductPreferenceKeyWords(keyWords);
    }

    /* Если пришедший из iiko продукт уже сохранен в базе, то обновляем ему наши старые настройки предпочтений */
    private static void updatePreferencesState(Product newProduct, Product existingProduct) {
        if (Objects.isNull(existingProduct)) {
            return;
        }
        newProduct.setWithNuts(existingProduct.isWithNuts());
        newProduct.setSpicy(existingProduct.isSpicy());
        newProduct.setVegetarian(existingProduct.isVegetarian());
        newProduct.getProductPreferenceKeyWords().addAll(existingProduct.getProductPreferenceKeyWords());
    }

    private static void setImage(Product newProduct, List<ImageInfo> dtoImages) {
        if (dtoImages.isEmpty()) {
            return;
        }
        /*Берем последнее добавленное в айко фото продукта*/
        newProduct.setImage(dtoImages.get(dtoImages.size() - 1).getImageUrl());
    }

    /**
     * DTO to Entity
     */

    public static List<DeliveryZone> toDeliveryZoneEntityList(List<IikoDeliveryZoneDto> dtoList) {
        return dtoList
                .stream()
                .map(IikoMappers::toDeliveryZoneEntity)
                .collect(Collectors.toList());
    }

    private static DeliveryZone toDeliveryZoneEntity(IikoDeliveryZoneDto dto) {
        DeliveryZone deliveryZone = new DeliveryZone();
        deliveryZone.setName(dto.getName());
        deliveryZone.setCoordinates(toDeliveryZoneEntityList(deliveryZone, dto.getCoordinates()));
        return deliveryZone;
    }

    private static List<DeliveryCoordinate> toDeliveryZoneEntityList(DeliveryZone zone, List<IikoDeliveryCoordinateDto> coordinateDtos) {
        // не используем стримы и foreach, так как нужно проставить порядок следования координат
        List<DeliveryCoordinate> result = new ArrayList<>();
        for (int i = 0; i < coordinateDtos.size(); i++) {
            result.add(toDeliveryZoneEntity(zone, coordinateDtos.get(i), i));
        }

        return result;
    }

    private static DeliveryCoordinate toDeliveryZoneEntity(DeliveryZone zone, IikoDeliveryCoordinateDto dto, int locationOrder) {
        DeliveryCoordinate coordinate = MAPPER.map(dto, DeliveryCoordinate.class);
        coordinate.setLocationOrder(locationOrder);
        return coordinate;
    }

}

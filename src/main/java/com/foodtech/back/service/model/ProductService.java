package com.foodtech.back.service.model;

import com.foodtech.back.dto.iiko.IikoNomenclatureDto;
import com.foodtech.back.dto.iiko.IikoProductCategoryDto;
import com.foodtech.back.dto.iiko.IikoProductDto;
import com.foodtech.back.dto.model.AdminProductPreferencesDto;
import com.foodtech.back.entity.model.iiko.Product;
import com.foodtech.back.entity.model.iiko.ProductCategory;
import com.foodtech.back.repository.iiko.ProductCategoryRepository;
import com.foodtech.back.repository.iiko.ProductRepository;
import com.foodtech.back.service.iiko.IikoRequestService;
import com.foodtech.back.util.mapper.IikoMappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.foodtech.back.util.mapper.IikoMappers.toProductEntities;

@Service
@Slf4j
public class ProductService {

    private final ProductCategoryRepository productCategoryRepository;

    private final ProductRepository productRepository;

    private final UserPreferencesService preferencesService;

    private final IikoRequestService iikoRequestService;

    public ProductService(ProductCategoryRepository productCategoryRepository, ProductRepository productRepository,
                          UserPreferencesService preferencesService, IikoRequestService iikoRequestService) {
        this.productCategoryRepository = productCategoryRepository;
        this.productRepository = productRepository;
        this.preferencesService = preferencesService;
        this.iikoRequestService = iikoRequestService;
    }

    @Cacheable(CacheService.PRODUCTS_FOR_FRONT_APP)
    public List<ProductCategory> getForFrontApp(Long userId) {
        List<ProductCategory> currentMenu = productCategoryRepository.findCurrentMenu();
        /* Применяем предпочтения юзера к меню */
        currentMenu.forEach(category -> preferencesService.applyPreferences(category.getProducts(), userId));
        return currentMenu;
    }

    public Map<Product, Integer> getProductsQuantity(Map<String, Integer> productsIdQuantity) {
        List<Product> products = productRepository.findByIdInAndInStopListFalseAndIncludedInMenuTrue(productsIdQuantity.keySet());
        return products.stream().collect(Collectors.toMap(Function.identity(), p -> productsIdQuantity.get(p.getId())));
    }

    public List<Product> getForAdmin() {
        return productRepository.findAllByOrderByIncludedInMenuDescProductCategoryNameAsc();
    }

    public Product getForAdmin(String id) {
        return productRepository.findById(id).orElseThrow();
    }

    public IikoNomenclatureDto getNomenclature() {
        return iikoRequestService.getNomenclature();
    }

    @Transactional
    @CacheEvict(value = CacheService.PRODUCTS_FOR_FRONT_APP, allEntries = true)
    public void saveNomenclature() {

        log.info("Updating iiko nomenclature");
        IikoNomenclatureDto nomenclature = getNomenclature();

        /* Сначала сохраняем категории блюд */
        Set<IikoProductCategoryDto> productCategoryDtoSet = nomenclature.getProductCategories();
        Set<ProductCategory> productCategories = IikoMappers.toCategoryEntities(productCategoryDtoSet);
        productCategoryRepository.saveAll(productCategories);

        /* Сохраняем продукты, сопостовляя их с ранее сохраненными категориями,
        *  а также сохраняем их предыдущее состояние в базе в части предпочтений */
        Set<IikoProductDto> productsFromIiko = nomenclature.getProducts();
        List<String> productsIdFromIiko = productsFromIiko
                .stream()
                .map(IikoProductDto::getId)
                .collect(Collectors.toList());
        List<Product> productsFromDatabase = productRepository.findByIdIn(productsIdFromIiko);
        Set<Product> newProducts = toProductEntities(productsFromIiko, productsFromDatabase, productCategories);
        productRepository.saveAll(newProducts);

        /* Снимаем флаг "is_included_in_menu" у всех уже существующих продуктов в базе,
           которые не указаны в текущем выгруженном меню, таким образом, продукты, которые в настоящий момент не выгружены в iiko,
           остаются в базе, но не отдаются в качестве текущего меню на фронт */
        productRepository.refreshMenu(newProducts.stream().map(Product::getId).collect(Collectors.toSet()));
    }

    @Transactional
    @CacheEvict(value = CacheService.PRODUCTS_FOR_FRONT_APP, allEntries = true)
    public void updateStopList() {
        log.debug("Updating stop list");

        Set<String> stopListItems = iikoRequestService.getStopList();
        if (Objects.isNull(stopListItems)) {
            log.error("Stop list updating failed");
            return;
        }

        log.info("In stop list {} items", stopListItems.size());

        if (stopListItems.isEmpty()) {
            productRepository.refreshStopList();
        } else {
            productRepository.updateStopList(stopListItems);
            productRepository.refreshStopList(stopListItems);
        }
    }

    @Transactional
    @CacheEvict(value = CacheService.PRODUCTS_FOR_FRONT_APP, allEntries = true)
    public void updatePreferences(AdminProductPreferencesDto productPreferences) {
        Product product = productRepository.findById(productPreferences.getProductId()).orElseThrow();
        product.setVegetarian(productPreferences.isVegetarian());
        product.setSpicy(productPreferences.isSpicy());
        product.setWithNuts(productPreferences.isWithNuts());
    }

    @Transactional
    @CacheEvict(value = CacheService.PRODUCTS_FOR_FRONT_APP, allEntries = true)
    public void addProductKeyWord(String wordToAdd, String id) {
        Product product = productRepository.findById(id).orElseThrow();
        product.getProductPreferenceKeyWords().add(wordToAdd);
    }

    @Transactional
    @CacheEvict(value = CacheService.PRODUCTS_FOR_FRONT_APP, allEntries = true)
    public void deletePreferencesKeyWord(String wordToDelete, String id) {
        Product product = productRepository.findById(id).orElseThrow();
        product.getProductPreferenceKeyWords().remove(wordToDelete);
    }
}

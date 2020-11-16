package com.foodtech.back.controller.admin;

import com.foodtech.back.dto.admin.ExternalIikoProductCategoryDto;
import com.foodtech.back.dto.iiko.IikoNomenclatureDto;
import com.foodtech.back.dto.iiko.IikoProductCategoryDto;
import com.foodtech.back.dto.iiko.IikoProductDto;
import com.foodtech.back.dto.model.AdminProductPreferencesDto;
import com.foodtech.back.entity.model.PreferencesKeyWord;
import com.foodtech.back.entity.model.iiko.Product;
import com.foodtech.back.service.model.ProductService;
import com.foodtech.back.service.model.UserPreferencesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class AdminProductController {

    private final ProductService productService;

    private final UserPreferencesService preferencesService;

    public AdminProductController(ProductService productService, UserPreferencesService preferencesService) {
        this.productService = productService;
        this.preferencesService = preferencesService;
    }

    @GetMapping(path = "/admin/products/nomenclature")
    public String saveNomenclature(RedirectAttributes redirectAttributes) {
        productService.saveNomenclature();
        redirectAttributes.addFlashAttribute("message", "Сохранено");
        return "redirect:/admin/products";
    }

    @GetMapping(path = "/admin/products/iiko")
    public String getExternalIikoMenu(Model model) {
        IikoNomenclatureDto nomenclature = productService.getNomenclature();
        Set<IikoProductCategoryDto> productCategories = nomenclature.getProductCategories();
        Set<IikoProductDto> products = nomenclature.getProducts();

        List<ExternalIikoProductCategoryDto> categoriesDto = productCategories
                .stream().map(pc -> new ExternalIikoProductCategoryDto(pc.getId(), pc.getName())).collect(Collectors.toList());

        Map<String, ExternalIikoProductCategoryDto> categoryMap = categoriesDto
                .stream().collect(Collectors.toMap(ExternalIikoProductCategoryDto::getId, Function.identity()));

        for (IikoProductDto product : products) {
            if (Objects.isNull(product.getId())) {
                continue;
            }

            ExternalIikoProductCategoryDto productCategoryDto = categoryMap.get(product.getProductCategoryId());
            if (Objects.isNull(productCategoryDto.getProducts())) {
                productCategoryDto.setProducts(new ArrayList<>());
            }

            productCategoryDto.getProducts().add(product.getName());
        }

        model.addAttribute("categories", categoriesDto);

        categoriesDto.removeIf(c -> Objects.isNull(c.getProducts()));
        return "products/externalIikoProducts";
    }


    @GetMapping(path = "/admin/products")
    public String getProducts(Model model) {
        List<Product> products = productService.getForAdmin();
        model.addAttribute("products", products);
        return "products/productList";
    }

    @GetMapping(path = "/admin/products/{id}/preferences")
    public String getPreferences(@PathVariable String id, Model model) {
        Product product = productService.getForAdmin(id);

        AdminProductPreferencesDto preferencesDto = new AdminProductPreferencesDto();
        preferencesDto.setProductId(product.getId());
        preferencesDto.setProductName(product.getName());
        preferencesDto.setVegetarian(product.isVegetarian());
        preferencesDto.setSpicy(product.isSpicy());
        preferencesDto.setWithNuts(product.isWithNuts());
        preferencesDto.setProductPreferenceKeyWords(product.getProductPreferenceKeyWords());

        model.addAttribute("productPreferences", preferencesDto);
        return "products/productPreferences";
    }

    @PostMapping(path = "/admin/products/preferences")
    public String savePreferences(@ModelAttribute AdminProductPreferencesDto productPreferences,
                                  RedirectAttributes redirectAttributes) {
        productService.updatePreferences(productPreferences);
        redirectAttributes.addFlashAttribute("message", "Обновлено");
        return "redirect:/admin/products/" + productPreferences.getProductId() + "/preferences";
    }

    @GetMapping(path = "/admin/products/{id}/words")
    public String getKeyWords(@PathVariable String id, Model model) {
        List<PreferencesKeyWord> allKeyWords = preferencesService.getAllKeyWordsForAdmin();
        Product product = productService.getForAdmin(id);
        Set<String> productKeyWords = product.getProductPreferenceKeyWords();
        model.addAttribute("productId", product.getId());
        model.addAttribute("productName", product.getName());
        model.addAttribute("allKeyWords", allKeyWords);
        model.addAttribute("productKeyWords", productKeyWords);
        return "products/productKeyWords";
    }

    @PostMapping(path = "/admin/products/{id}/words")
    public String addProductKeyWord(@RequestParam String wordToAdd, @PathVariable String id) {
        productService.addProductKeyWord(wordToAdd, id);
        return "redirect:/admin/products/" + id + "/words";
    }

    @PostMapping(path = "/admin/products/{id}/words/delete")
    public String deleteKeyWord(@RequestParam String wordToDelete, @PathVariable String id) {
        productService.deletePreferencesKeyWord(wordToDelete, id);
        return "redirect:/admin/products/" + id + "/words";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Model model) {
        model.addAttribute("url", "/admin/products");
        return "products/error";
    }
}

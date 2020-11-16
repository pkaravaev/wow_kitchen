package com.foodtech.back.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.foodtech.back.entity.model.iiko.Product;
import com.foodtech.back.entity.model.iiko.ProductCategory;
import com.foodtech.back.util.DateUtil;
import com.foodtech.back.util.ResponseCode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static com.foodtech.back.IntegrationTestData.KITCHEN_CLOSED_TIME;
import static com.foodtech.back.IntegrationTestData.KITCHEN_OPEN_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"classpath:delete-product-test-data.sql", "classpath:user-data.sql", "classpath:product-test-data.sql"})
@Sql(scripts = {"classpath:delete-product-test-data.sql"}, executionPhase = AFTER_TEST_METHOD)
class ProductControllerTest extends AbstractControllerIntegrationTest {

    @MockBean
    private DateUtil dateUtil;

    @Test
    void getProducts() throws Exception {

        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_OPEN_TIME);

        MvcResult result = mockMvc.perform(get("/app/products")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user2AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<ProductCategory> productsFromCategoriesResponse = mapResult(result.getResponse().getContentAsString(), new TypeReference<>() {});
        List<Product> productsFromResponse = new ArrayList<>();
        for (ProductCategory category : productsFromCategoriesResponse) {
            productsFromResponse.addAll(category.getProducts());
        }

        assertGetProductsResponseIsValid(productsFromResponse);
        assertUserPreferencesAppliedSuccessfully(productsFromResponse);
    }

    private void assertGetProductsResponseIsValid(List<Product> products) {
        assertThat(products).containsAll(testData.testProductIncludedData());
        assertThat(products).doesNotContainAnyElementsOf(testData.testProductNotIncludedData());
    }

    private void assertUserPreferencesAppliedSuccessfully(List<Product> products) {
        Product productWithPreferences = products.stream()
                .filter(p -> testData.product1().getId().equals(p.getId())).findFirst().orElseThrow();
        assertTrue(productWithPreferences.getUserPreferences().isShowVegetarian());
        assertTrue(productWithPreferences.getUserPreferences().isShowSpicy());
        assertTrue(productWithPreferences.getUserPreferences().isShowDontLike());
    }

    @Test
    void getProductsKitchenClosed() throws Exception {

        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_CLOSED_TIME);

        mockMvc.perform(get("/app/products")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.KITCHEN_CLOSED.toString()));
    }
}
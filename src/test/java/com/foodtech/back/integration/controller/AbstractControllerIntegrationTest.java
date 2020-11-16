package com.foodtech.back.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtech.back.IntegrationTestData;
import com.foodtech.back.repository.iiko.ProductCategoryRepository;
import com.foodtech.back.repository.iiko.ProductRepository;
import com.foodtech.back.repository.model.PreferencesKeyWordsRepository;
import com.foodtech.back.repository.model.UserPreferencesRepository;
import com.foodtech.back.repository.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringJUnitWebConfig
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(value = {
        "classpath:sms.properties",
        "classpath:crypt.properties",
        "classpath:iiko.properties",
        "classpath:delivery.properties",
        "classpath:push.properties",
        "classpath:payment.properties",
        "classpath:client_messages.properties",
        "classpath:rabbitmq.properties",

        "classpath:payment-dev.properties"
})
public abstract class AbstractControllerIntegrationTest {

    MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public UserPreferencesRepository userPreferencesRepository;

    @Autowired
    public PreferencesKeyWordsRepository keyWordsRepository;

    @Autowired
    public ProductCategoryRepository productCategoryRepository;

    @Autowired
    public ProductRepository productRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    public IntegrationTestData testData;

    @PostConstruct
    private void postConstruct() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    <T> T mapResult(String content, TypeReference<T> type) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(content);
        JsonNode body = jsonNode.findValue("body");
        return objectMapper.readValue(body.toString(), type);
    }

}

package com.foodtech.back.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.foodtech.back.dto.model.OrderInfoDto;
import com.foodtech.back.dto.model.OrderItemDto;
import com.foodtech.back.dto.model.OrderRegistrationDto;
import com.foodtech.back.dto.model.OrderRegistrationItemDto;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.OrderStatus;
import com.foodtech.back.repository.model.OrderItemRepository;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.service.notification.ampq.RabbitMqQueueType;
import com.foodtech.back.service.notification.ampq.RabbitMqService;
import com.foodtech.back.util.DateUtil;
import com.foodtech.back.util.ResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.foodtech.back.IntegrationTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"classpath:delete-order-test-data.sql","classpath:delete-product-test-data.sql",
        "classpath:user-data.sql", "classpath:product-test-data.sql", "classpath:order-test-data.sql"})
@Sql(scripts = {"classpath:delete-order-test-data.sql", "classpath:delete-product-test-data.sql"}, executionPhase = AFTER_TEST_METHOD)
class OrderControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @MockBean
    public RabbitMqService rabbitMqService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @MockBean
    private DateUtil dateUtil;

    @BeforeEach
    void setUp() {
        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_OPEN_TIME);
    }

    @Test
    void getOrder() throws Exception {
        MvcResult result = mockMvc.perform(get("/app/order/" + testData.order1().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertGetOrderResponseIsValid(result.getResponse().getContentAsString());
    }

    private void assertGetOrderResponseIsValid(String responseContent) throws IOException {
        OrderInfoDto responseOrderDto = mapResult(responseContent, new TypeReference<>() {});
        assertThat(responseOrderDto).isEqualToIgnoringGivenFields(testData.order1InfoDto(), "orderItems", "bankCard",
                "address", "created", "updated");

        assertEquals(responseOrderDto.getBankCard().getCardMask(), testData.bankCard1().getCardMask());
        assertEquals(responseOrderDto.getAddress(), testData.address1());

        List<OrderItemDto> dtoOrderItems = responseOrderDto.getOrderItems();
        assertThat(dtoOrderItems).containsExactlyInAnyOrder(testData.order1_ItemDto1(), testData.order1_ItemDto2());
    }

    @Test
    void getAllOrdersOfUser() throws Exception {
        MvcResult result = mockMvc.perform(get("/app/order")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertGetAllOrderOfUserResponseIsValid(result.getResponse().getContentAsString());
    }

    void assertGetAllOrderOfUserResponseIsValid(String responseContent) throws IOException {
        List<OrderInfoDto> responseDtoList = mapResult(responseContent, new TypeReference<>() {});
        assertThat(responseDtoList).usingElementComparatorIgnoringFields("orderItems", "bankCard", "address", "updated")
                .containsExactly(testData.order2InfoDto(), testData.order1InfoDto());

        OrderInfoDto order1Dto = responseDtoList.get(1);
        assertEquals(order1Dto.getBankCard().getCardMask(), testData.bankCard1().getCardMask());
        assertEquals(order1Dto.getAddress(), testData.address1());
        List<OrderItemDto> order1DtoItems = order1Dto.getOrderItems();
        assertThat(order1DtoItems).containsExactlyInAnyOrder(testData.order1_ItemDto1(), testData.order1_ItemDto2());

        OrderInfoDto order2Dto = responseDtoList.get(0);
        assertEquals(order2Dto.getBankCard().getCardMask(), testData.bankCard2().getCardMask());
        assertEquals(order2Dto.getAddress(), testData.address2());
        List<OrderItemDto> order2DtoItems = order2Dto.getOrderItems();
        assertThat(order2DtoItems).containsExactlyInAnyOrder(testData.order2_ItemDto3(), testData.Order2_ItemDto4(),
                testData.order2_ItemDto5());
    }

    @Test
    void getInProcessing() throws Exception {
        MvcResult result = mockMvc.perform(get("/app/order/processing")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", testData.user1AuthTokenHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertGetOrderInProcessingResponseIsValid(result.getResponse().getContentAsString());
    }

    private void assertGetOrderInProcessingResponseIsValid(String responseContent) throws IOException {
        OrderInfoDto orderDto = mapResult(responseContent, new TypeReference<>() {});
        assertEquals(Long.valueOf(1), orderDto.getId());
        assertEquals(testData.order1().getStatusQueueName(), orderDto.getStatusQueueName());
        assertEquals(testData.order1().getDeliveryTime(), orderDto.getTotalDeliveryTime());
    }

    @Test
    void register() throws Exception {
        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_OPEN_TIME);
        when(rabbitMqService.createQueue(any(RabbitMqQueueType.class), anyString())).thenReturn(TEST_QUEUE_NAME);

        MvcResult response = mockMvc.perform(post("/app/order")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.orderRegistrationDto())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        OrderInfoDto responseDto = mapResult(response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertRegisterOrderResponseIsValid(responseDto);
        assertNewOrderSaved(responseDto.getId());
    }

    private void assertRegisterOrderResponseIsValid(OrderInfoDto responseDto) {
        assertNotNull(responseDto.getId());
        assertEquals(testData.orderRegistrationResponseDto().getStatusQueueName(), responseDto.getStatusQueueName());
        assertEquals(testData.orderRegistrationResponseDto().getTotalDeliveryTime(), responseDto.getTotalDeliveryTime());
    }

    private void assertNewOrderSaved(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.NEW, order.getStatus());
        assertEquals(testData.orderRegistrationDto().getTotalCost(), order.getTotalCost());

        List<String> itemsIdFromTestData = testData.orderRegistrationDto().getOrderItems().stream().map(OrderRegistrationItemDto::getProductId).collect(Collectors.toList());
        List<String> itemsIdFromCreatedOrder = orderItemRepository.findAllByOrderId(orderId).stream().map(item -> item.getProduct().getId()).collect(Collectors.toList());
        assertThat(itemsIdFromCreatedOrder).containsAll(itemsIdFromTestData);
    }

    @Test
    void registerKitchenClosed() throws Exception {
        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_CLOSED_TIME);
        when(rabbitMqService.createQueue(any(RabbitMqQueueType.class), anyString())).thenReturn(TEST_QUEUE_NAME);

        mockMvc.perform(post("/app/order")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData.orderRegistrationDto())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.KITCHEN_CLOSED.toString()));

        assertNewOrderNotSaved();
    }

    @Test
    void registerWrongTotalCost() throws Exception {
        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_OPEN_TIME);
        when(rabbitMqService.createQueue(any(RabbitMqQueueType.class), anyString())).thenReturn(TEST_QUEUE_NAME);

        /* Ставим неправильно высчитанную сумму корзину (не учтены бонусы юзера) */
        OrderRegistrationDto registrationDto = testData.orderRegistrationDto();
        registrationDto.setTotalCost(200);

        mockMvc.perform(post("/app/order")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.CART_INVALID.toString()));

        assertNewOrderNotSaved();
    }

    @Test
    void registerWrongOrderItems() throws Exception {
        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_OPEN_TIME);
        when(rabbitMqService.createQueue(any(RabbitMqQueueType.class), anyString())).thenReturn(TEST_QUEUE_NAME);

        /* Ставим несуществующий ID продукта */
        OrderRegistrationDto registrationDto = new OrderRegistrationDto();
        registrationDto.setTotalCost(100);
        OrderRegistrationItemDto itemDto = new OrderRegistrationItemDto();
        itemDto.setProductId("9999");
        itemDto.setAmount(1);
        registrationDto.setOrderItems(List.of(itemDto));

        mockMvc.perform(post("/app/order")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.CART_INVALID.toString()));

        assertNewOrderNotSaved();
    }

    @Test
    void registerWrongFrontDtoTotalCost() throws Exception {
        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_OPEN_TIME);
        when(rabbitMqService.createQueue(any(RabbitMqQueueType.class), anyString())).thenReturn(TEST_QUEUE_NAME);

        OrderRegistrationDto registrationDto = testData.orderRegistrationDto();
        registrationDto.setTotalCost(null);

        mockMvc.perform(post("/app/order")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.REQUEST_INVALID.toString()));
    }

    @Test
    void registerWrongFrontDtoTotalCostZero() throws Exception {
        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_OPEN_TIME);
        when(rabbitMqService.createQueue(any(RabbitMqQueueType.class), anyString())).thenReturn(TEST_QUEUE_NAME);

        OrderRegistrationDto registrationDto = testData.orderRegistrationDto();
        registrationDto.setTotalCost(0);

        mockMvc.perform(post("/app/order")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.REQUEST_INVALID.toString()));

        assertNewOrderNotSaved();
    }

    @Test
    void registerWrongFrontDtoEmptyItems() throws Exception {
        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_OPEN_TIME);
        when(rabbitMqService.createQueue(any(RabbitMqQueueType.class), anyString())).thenReturn(TEST_QUEUE_NAME);

        OrderRegistrationDto registrationDto = testData.orderRegistrationDto();
        registrationDto.setOrderItems(Collections.emptyList());

        mockMvc.perform(post("/app/order")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.REQUEST_INVALID.toString()));

        assertNewOrderNotSaved();
    }

    @Test
    void registerWrongFrontDtoZeroItemAmount() throws Exception {
        when(dateUtil.getCurrentDayForTimeZone(anyString())).thenReturn(KITCHEN_OPEN_TIME);
        when(rabbitMqService.createQueue(any(RabbitMqQueueType.class), anyString())).thenReturn(TEST_QUEUE_NAME);

        OrderRegistrationDto registrationDto = testData.orderRegistrationDto();
        OrderRegistrationItemDto itemDto = registrationDto.getOrderItems().get(0);
        itemDto.setAmount(0);

        mockMvc.perform(post("/app/order")
                .header("Authorization", testData.user1AuthTokenHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseCode.REQUEST_INVALID.toString()));

        assertNewOrderNotSaved();
    }

    private void assertNewOrderNotSaved() {
        List<Order> allOrders = orderRepository.findAll();
        assertEquals(TEST_ORDERS_TOTAL_AMOUNT, allOrders.size());
    }

}
package com.foodtech.back.unit.dto;

import com.foodtech.back.dto.iiko.IikoAddressDto;
import com.foodtech.back.dto.iiko.IikoOrderItem;
import com.foodtech.back.dto.iiko.IikoOrderRequest;
import com.foodtech.back.dto.iiko.IikoPaymentType;
import com.foodtech.back.entity.model.*;
import com.foodtech.back.entity.model.iiko.Product;
import com.foodtech.back.util.mapper.OrderMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.foodtech.back.dto.iiko.IikoOrderRequest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IikoOrderRequestUnitTest {

    @Test
    void form() {
        //when
        Order order = order();
        KitchenDeliveryTerminal deliveryTerminal = order.getAddress().getDeliveryTerminal();

        //do
        IikoOrderRequest request = IikoOrderRequest.form(order);

        //then
        assertFormedCorrectly(request, deliveryTerminal, order, order.getUser().getName(), order.getAddress().getHome(), null);
    }

    @Test
    void formNoName() {
        //when
        Order order = order();
        order.getUser().setName(null);
        KitchenDeliveryTerminal deliveryTerminal = order.getAddress().getDeliveryTerminal();

        //do
        IikoOrderRequest request = IikoOrderRequest.form(order);

        //then
        assertFormedCorrectly(request, deliveryTerminal, order, NO_NAME_USER, order.getAddress().getHome(), null);
    }

    @Test
    void formTooLongHome() {
        //when
        Order order = order();
        order.getAddress().setHome("H".repeat(IIKO_HOME_MAX_LENGTH + 1));
        KitchenDeliveryTerminal deliveryTerminal = order.getAddress().getDeliveryTerminal();
        Kitchen kitchen = deliveryTerminal.getKitchen();

        //do
        IikoOrderRequest request = IikoOrderRequest.form(order);

        //then
        assertFormedCorrectly(request, deliveryTerminal, order, order.getUser().getName(), TOO_LONG_HOME, order.getAddress().getHome());
    }

    @Test
    void formWithCutlery() {
        //when
        Order order = order();
        order.setCutlery(true);
        KitchenDeliveryTerminal deliveryTerminal = order.getAddress().getDeliveryTerminal();
        Kitchen kitchen = deliveryTerminal.getKitchen();

        //do
        IikoOrderRequest request = IikoOrderRequest.form(order);

        //then
        assertEquals(request.getOrganization(), kitchen.getOrganizationId());
        assertEquals(request.getDeliveryTerminalId(), deliveryTerminal.getTerminalId());
        IikoPaymentType requestPaymentType = request.getOrder().getPaymentItems().get(0).getPaymentType();
        assertEquals(requestPaymentType.getId(), kitchen.getPaymentTypeId());
        assertEquals(requestPaymentType.getCode(), kitchen.getPaymentTypeCode());
        assertEquals(requestPaymentType.getName(), kitchen.getPaymentTypeName());
        assertEquals(Long.valueOf(requestPaymentType.getExternalRevision()), kitchen.getPaymentTypeExternalRevision());
        assertEquals(requestPaymentType.isCombinable(), kitchen.isPaymentTypeCombinable());
        assertEquals(requestPaymentType.isDeleted(), kitchen.isPaymentTypeDeleted());
        assertEquals(request.getCustomer().getPhone(), order.getUser().toString());
        assertEquals(request.getCustomer().getName(), order.getUser().getName());
        assertEquals(request.getOrder().getPhone(), order.getUser().toString());
        assertEquals(Integer.valueOf(request.getOrder().getPaymentItems().get(0).getSum()), order.getTotalCost());
        IikoAddressDto address = request.getOrder().getAddress();
        assertEquals(address.getCity(), order.getAddress().getCity());
        assertEquals(address.getStreet(), order.getAddress().getStreet());
        assertEquals(address.getHome(), order.getAddress().getHome());

        List<IikoOrderItem> iikoOrderItems = request.getOrder().getItems();
        Assertions.assertThat(iikoOrderItems).containsAll(OrderMapper.toIikoOrderItems(List.of(item1(), item2())));
        Assertions.assertThat(iikoOrderItems).contains(CUTLERY);
    }

    private void assertFormedCorrectly(IikoOrderRequest request, KitchenDeliveryTerminal deliveryTerminal, Order order,
                                       String expectedName, String expectedHome, String expectedComment) {
        Kitchen kitchen = deliveryTerminal.getKitchen();
        assertEquals(request.getOrganization(), kitchen.getOrganizationId());
        assertEquals(request.getDeliveryTerminalId(), deliveryTerminal.getTerminalId());
        IikoPaymentType requestPaymentType = request.getOrder().getPaymentItems().get(0).getPaymentType();
        assertEquals(requestPaymentType.getId(), kitchen.getPaymentTypeId());
        assertEquals(requestPaymentType.getCode(), kitchen.getPaymentTypeCode());
        assertEquals(requestPaymentType.getName(), kitchen.getPaymentTypeName());
        assertEquals(Long.valueOf(requestPaymentType.getExternalRevision()), kitchen.getPaymentTypeExternalRevision());
        assertEquals(requestPaymentType.isCombinable(), kitchen.isPaymentTypeCombinable());
        assertEquals(requestPaymentType.isDeleted(), kitchen.isPaymentTypeDeleted());
        assertEquals(request.getCustomer().getPhone(), order.getUser().toString());
        assertEquals(request.getCustomer().getName(), expectedName);
        assertEquals(request.getOrder().getPhone(), order.getUser().toString());
        assertEquals(Integer.valueOf(request.getOrder().getPaymentItems().get(0).getSum()), order.getTotalCost());
        IikoAddressDto address = request.getOrder().getAddress();
        assertEquals(address.getCity(), order.getAddress().getCity());
        assertEquals(address.getStreet(), order.getAddress().getStreet());
        assertEquals(address.getHome(), expectedHome);
        assertEquals(request.getOrder().getAddress().getComment(), expectedComment);
        List<IikoOrderItem> iikoOrderItems = request.getOrder().getItems();
        Assertions.assertThat(iikoOrderItems).containsAll(OrderMapper.toIikoOrderItems(List.of(item1(), item2())));
    }


    private static Order order() {
        Order order = new Order();
        order.setTotalCost(999);
        order.setItems(List.of(item1(), item2()));
        User user = user();
        Address address = address();
        order.setUser(user);
        order.setAddress(address);
        return order;
    }

    private static OrderItem item1() {
        OrderItem item = new OrderItem();
        item.setAmount(1);
        Product product = new Product();
        product.setId("PRODUCT_1_ID");
        product.setName("PRODUCT_1");
        item.setProduct(product);
        return item;
    }

    private static OrderItem item2() {
        OrderItem item = new OrderItem();
        item.setAmount(1);
        Product product = new Product();
        product.setId("PRODUCT_2_ID");
        product.setName("PRODUCT_2");
        item.setProduct(product);
        return item;
    }

    private static Address address() {
        Address address = new Address();
        address.setId(1L);
        address.setCity("Нур-Султан");
        address.setStreet("улица Жубанова");
        address.setHome("1");
        address.setHousing("2A");
        address.setEntrance("3");
        address.setDoorphone("58K6709");
        address.setFloor("4");
        address.setApartment("77");
        address.setComment("Вход со двора");
        address.setActual(true);

        KitchenDeliveryTerminal terminal = new KitchenDeliveryTerminal();
        terminal.setId(1L);
        Kitchen kitchen = new Kitchen();
        kitchen.setPaymentTypeId("PT_ID");
        kitchen.setPaymentTypeCode("PT_CODE");
        kitchen.setPaymentTypeName("PT_NAME");
        kitchen.setPaymentTypeExternalRevision(100L);
        kitchen.setPaymentTypeCombinable(true);
        terminal.setKitchen(kitchen);
        address.setDeliveryTerminal(terminal);
        return address;
    }

    private static User user() {
        User user = new User();
        user.setCountryCode("7");
        user.setMobileNumber("7777777777");
        user.setName("USER");
        return user;
    }

}
package com.foodtech.back.unit.service.model;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.iiko.IikoOrderInfo;
import com.foodtech.back.dto.iiko.IikoOrderSendingResult;
import com.foodtech.back.dto.model.OrderRegistrationDto;
import com.foodtech.back.dto.model.OrderRegistrationItemDto;
import com.foodtech.back.entity.bonus.BonusAccount;
import com.foodtech.back.entity.model.*;
import com.foodtech.back.entity.model.iiko.Product;
import com.foodtech.back.entity.payment.cloud.CloudPayment;
import com.foodtech.back.repository.model.OrderItemRepository;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.service.bonus.BonusService;
import com.foodtech.back.service.model.OrderServiceImpl;
import com.foodtech.back.service.model.ProductService;
import com.foodtech.back.service.model.UserServiceImpl;
import com.foodtech.back.service.notification.ampq.RabbitMqQueueType;
import com.foodtech.back.service.notification.ampq.RabbitMqService;
import com.foodtech.back.unit.AbstractUnitTest;
import com.foodtech.back.util.exceptions.CartInvalidException;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.foodtech.back.entity.model.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrderServiceImplTest extends AbstractUnitTest {

    @Autowired
    OrderServiceImpl orderService;

    @MockBean
    OrderRepository orderRepository;

    @MockBean
    OrderItemRepository orderItemRepository;

    @MockBean
    UserServiceImpl userService;

    @MockBean
    ProductService productService;

    @MockBean
    BonusService bonusService;

    @MockBean
    RabbitMqService rabbitMqService;

    @Autowired
    ResourcesProperties properties;

    @Test
    void register_case1() {
        OrderRegistrationDto dto = registrationDto1();
        User user = user();
        when(userService.getWithActualAddress(user.getId())).thenReturn(user);
        when(productService.getProductsQuantity(dto.getItemsQuantity())).thenReturn(productQuantity1());
        Integer totalCost = dto.getTotalCost();
        when(bonusService.countCartCostWithBonuses(totalCost, user.getBonusAccount().getBonusAmount())).thenReturn(totalCost);
        when(rabbitMqService.createQueue(eq(RabbitMqQueueType.ORDER_STATUS_QUEUE), anyString())).thenReturn(QUEUE_NAME);
        when(orderRepository.save(any(Order.class))).thenAnswer((Answer<Order>) invocation -> (Order) invocation.getArgument(0));

        Order order = orderService.register(1L, dto);

        assertOrderCreatedCorrect(order, dto.getTotalCost());
        assertOrderItemsCreatedCorrect1(order);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void register_case2() {
        OrderRegistrationDto dto = registrationDto2();
        User user = user();
        when(userService.getWithActualAddress(user.getId())).thenReturn(user);
        when(productService.getProductsQuantity(dto.getItemsQuantity())).thenReturn(productQuantity2());
        Integer totalCost = dto.getTotalCost();
        when(bonusService.countCartCostWithBonuses(totalCost, user.getBonusAccount().getBonusAmount())).thenReturn(totalCost);
        when(rabbitMqService.createQueue(eq(RabbitMqQueueType.ORDER_STATUS_QUEUE), anyString())).thenReturn(QUEUE_NAME);
        when(orderRepository.save(any(Order.class))).thenAnswer((Answer<Order>) invocation -> (Order) invocation.getArgument(0));

        Order order = orderService.register(1L, dto);

        assertOrderCreatedCorrect(order, dto.getTotalCost());
        assertOrderItemsCreatedCorrect2(order);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void register_withBonuses() {
        int appliedBonuses = 100;
        OrderRegistrationDto dto = registrationDto1();
        dto.setTotalCost(dto.getTotalCost() - appliedBonuses);
        User user = user();
        when(userService.getWithActualAddress(user.getId())).thenReturn(user);
        when(productService.getProductsQuantity(dto.getItemsQuantity())).thenReturn(productQuantity1());
        int costWithoutBonuses = totalCostQuantity1();
        when(bonusService.countCartCostWithBonuses(costWithoutBonuses, user.getBonusAccount().getBonusAmount())).thenReturn(costWithoutBonuses - appliedBonuses);
        when(rabbitMqService.createQueue(eq(RabbitMqQueueType.ORDER_STATUS_QUEUE), anyString())).thenReturn(QUEUE_NAME);
        when(orderRepository.save(any(Order.class))).thenAnswer((Answer<Order>) invocation -> (Order) invocation.getArgument(0));

        Order order = orderService.register(1L, dto);

        assertOrderCreatedCorrectWithBonuses(order, dto.getTotalCost(), totalCostQuantity1());
        assertOrderItemsCreatedCorrect1(order);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void register_wrongBonusesApplied() {
        int appliedBonuses = 100;
        OrderRegistrationDto dto = registrationDto1();
        dto.setTotalCost(dto.getTotalCost());
        User user = user();
        when(userService.getWithActualAddress(user.getId())).thenReturn(user);
        when(productService.getProductsQuantity(dto.getItemsQuantity())).thenReturn(productQuantity1());
        int costWithoutBonuses = totalCostQuantity1();
        when(bonusService.countCartCostWithBonuses(costWithoutBonuses, user.getBonusAccount().getBonusAmount())).thenReturn(costWithoutBonuses - appliedBonuses);
        when(rabbitMqService.createQueue(eq(RabbitMqQueueType.ORDER_STATUS_QUEUE), anyString())).thenReturn(QUEUE_NAME);
        when(orderRepository.save(any(Order.class))).thenAnswer((Answer<Order>) invocation -> (Order) invocation.getArgument(0));

        assertThrows(CartInvalidException.class, () -> orderService.register(1L, dto));
        verify(orderRepository, times(0)).save(any(Order.class));
    }

    @Test
    void register_productIdNotFoundInDb() {
        OrderRegistrationDto dto = registrationDto1();
        User user = user();
        when(userService.getWithActualAddress(user.getId())).thenReturn(user);
        Map<Product, Integer> nfQuantity = Map.of(product1(), PRODUCT_1_QUANTITY_1, product2(), PRODUCT_2_QUANTITY_1);
        when(productService.getProductsQuantity(dto.getItemsQuantity())).thenReturn(nfQuantity);
        Integer totalCost = dto.getTotalCost();
        when(bonusService.countCartCostWithBonuses(totalCost, user.getBonusAccount().getBonusAmount())).thenReturn(totalCost);
        when(rabbitMqService.createQueue(eq(RabbitMqQueueType.ORDER_STATUS_QUEUE), anyString())).thenReturn(QUEUE_NAME);
        when(orderRepository.save(any(Order.class))).thenAnswer((Answer<Order>) invocation -> (Order) invocation.getArgument(0));

        assertThrows(CartInvalidException.class, () -> orderService.register(user.getId(), dto));
        verify(orderRepository, times(0)).save(any(Order.class));
    }

    @Test
    void register_productIdDuplicate() {
        OrderRegistrationDto dto = registrationDto1();
        dto.setOrderItems(List.of(item1(), item1()));
        User user = user();
        when(userService.getWithActualAddress(user.getId())).thenReturn(user);
        Map<Product, Integer> nfQuantity = Map.of(product1(), PRODUCT_1_QUANTITY_1, product2(), PRODUCT_2_QUANTITY_1);
        when(productService.getProductsQuantity(anyMap())).thenReturn(nfQuantity);
        Integer totalCost = dto.getTotalCost();
        when(bonusService.countCartCostWithBonuses(totalCost, user.getBonusAccount().getBonusAmount())).thenReturn(totalCost);
        when(rabbitMqService.createQueue(eq(RabbitMqQueueType.ORDER_STATUS_QUEUE), anyString())).thenReturn(QUEUE_NAME);
        when(orderRepository.save(any(Order.class))).thenAnswer((Answer<Order>) invocation -> (Order) invocation.getArgument(0));

        assertThrows(CartInvalidException.class, () -> orderService.register(user.getId(), dto));
        verify(orderRepository, times(0)).save(any(Order.class));
    }

    @Test
    void processPaid() {
        Order order = order();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processPaid(order.getId());

        assertEquals(ON_SENDING, order.getStatus());

        assertFalse(order.isCheckStatus());
        assertTrue(order.isInProcessing());
        assertFalse(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processIikoSendingSuccess() {
        Order order = order();
        IikoOrderInfo orderInfo = new IikoOrderInfo();
        orderInfo.setOrderId("IIKO_ORDER_ID");
        orderInfo.setNumber("IIKO_SHORT_ID");
        IikoOrderSendingResult sendingResult = IikoOrderSendingResult.success(orderInfo);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processIikoSendingSuccess(order.getId(), sendingResult);

        assertEquals(SENT, order.getStatus());
        assertEquals(orderInfo.getOrderId(), order.getIikoOrderId());
        assertEquals(orderInfo.getNumber(), order.getIikoShortId());
        assertEquals(sendingResult.getProblem(), order.getIikoProblem());

        assertTrue(order.isCheckStatus());
        assertTrue(order.isInProcessing());
        assertFalse(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processIikoSendingFailed() {
        Order order = order();
        IikoOrderSendingResult sendingResult = IikoOrderSendingResult.failed("Some sending problem");
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processIikoSendingFail(order.getId(), sendingResult);

        assertEquals(SENDING_FAILED, order.getStatus());
        assertEquals(sendingResult.getProblem(), order.getIikoProblem());
        assertFalse(order.isCheckStatus());
        assertFalse(order.isInProcessing());
        assertTrue(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processInProgress() {
        Order order = order();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processInProgress(order.getId());

        assertEquals(IN_PROGRESS, order.getStatus());
        assertTrue(order.isCheckStatus());
        assertTrue(order.isInProcessing());
        assertFalse(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processReady() {
        Order order = order();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processReady(order.getId());

        assertEquals(READY, order.getStatus());
        assertTrue(order.isCheckStatus());
        assertTrue(order.isInProcessing());
        assertFalse(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processAwaitingDelivery() {
        Order order = order();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processAwaitingDelivery(order.getId());

        assertEquals(AWAITING_DELIVERY, order.getStatus());
        assertTrue(order.isCheckStatus());
        assertTrue(order.isInProcessing());
        assertFalse(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processOnTheWay() {
        Order order = order();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processOnTheWay(order.getId());

        assertEquals(ON_THE_WAY, order.getStatus());
        assertTrue(order.isCheckStatus());
        assertTrue(order.isInProcessing());
        assertFalse(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processDelivered() {
        Order order = order();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processDelivered(order.getId());

        assertEquals(DELIVERED, order.getStatus());
        assertTrue(order.isCheckStatus());
        assertFalse(order.isInProcessing());
        assertTrue(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processClosed() {
        Order order = order();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processClosed(order.getId());

        assertEquals(CLOSED, order.getStatus());
        assertFalse(order.isCheckStatus());
        assertFalse(order.isInProcessing());
        assertTrue(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processClosed_PrevStatusIsDelivered() {
        Order order = order();
        order.setStatus(DELIVERED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processClosed(order.getId());

        assertEquals(CLOSED, order.getStatus());
        assertFalse(order.isCheckStatus());
        assertFalse(order.isInProcessing());
        assertFalse(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processCancelled() {
        Order order = order();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processCancelled(order.getId());

        assertEquals(CANCELLED, order.getStatus());
        assertFalse(order.isCheckStatus());
        assertFalse(order.isInProcessing());
        assertTrue(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processNotConfirmed() {
        Order order = order();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processCancelled(order.getId());

        assertEquals(CANCELLED, order.getStatus());
        assertFalse(order.isCheckStatus());
        assertFalse(order.isInProcessing());
        assertTrue(order.getCloudPayment().isPaymentCompleteRequired());
    }

    @Test
    void processNotProcessed() {
        Order order = order();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.processNotProcessed(order.getId());

        assertEquals(order().getStatus(), order.getStatus());
        assertFalse(order.isCheckStatus());
        assertFalse(order.isInProcessing());
    }

    private void assertOrderCreatedCorrect(Order order, Integer totalCost) {
        assertNotNull(order);
        assertNotNull(order.getId());
        assertThat(order.getUser()).isEqualToComparingFieldByField(user());
        assertThat(order.getAddress()).isEqualToComparingFieldByField(user().getActualAddress());
        assertEquals(totalCost, order.getTotalCost());
        assertEquals(totalCost, order.getProductsCost());
        assertEquals(Integer.valueOf(0), order.getAppliedBonusAmount());
        assertEquals(NEW, order.getStatus());
        assertEquals(registrationDto1().isCutlery(), order.isCutlery());
        assertEquals(QUEUE_NAME, order.getStatusQueueName());
        assertEquals(properties.getDefaultDeliveryTime(), order.getDeliveryTime());
    }

    private void assertOrderCreatedCorrectWithBonuses(Order order, int totalCost, int productCost) {
        assertNotNull(order);
        assertNotNull(order.getId());
        assertThat(order.getUser()).isEqualToComparingFieldByField(user());
        assertThat(order.getAddress()).isEqualToComparingFieldByField(user().getActualAddress());
        assertEquals(Integer.valueOf(totalCost), order.getTotalCost());
        assertEquals(Integer.valueOf(productCost) , order.getProductsCost());
        assertEquals(Integer.valueOf(productCost - totalCost), order.getAppliedBonusAmount());
        assertEquals(NEW, order.getStatus());
        assertEquals(registrationDto1().isCutlery(), order.isCutlery());
        assertEquals(QUEUE_NAME, order.getStatusQueueName());
        assertEquals(properties.getDefaultDeliveryTime(), order.getDeliveryTime());
    }

    private void assertOrderItemsCreatedCorrect1(Order order) {
        List<OrderItem> items = order.getItems();
        assertEquals(productQuantity1().size(), items.size());
        for (OrderItem item : items) {
            String itemId = item.getProduct().getId();
            if (PRODUCT_1_ID.equals(itemId)) {
                assertEquals(PRODUCT_1_QUANTITY_1, item.getAmount());
            } else if (PRODUCT_2_ID.equals(itemId)) {
                assertEquals(PRODUCT_2_QUANTITY_1, item.getAmount());
            } else if (PRODUCT_3_ID.equals(itemId)) {
                assertEquals(PRODUCT_3_QUANTITY_1, item.getAmount());
            } else {
                fail();
            }
            assertThat(item.getOrder()).isEqualToComparingFieldByField(order);
        }
    }

    private void assertOrderItemsCreatedCorrect2(Order order) {
        List<OrderItem> items = order.getItems();
        assertEquals(productQuantity2().size(), items.size());
        for (OrderItem item : items) {
            String itemId = item.getProduct().getId();
            if (PRODUCT_1_ID.equals(itemId)) {
                assertEquals(PRODUCT_1_QUANTITY_2, item.getAmount());
            } else if (PRODUCT_2_ID.equals(itemId)) {
                assertEquals(PRODUCT_2_QUANTITY_2, item.getAmount());
            } else if (PRODUCT_3_ID.equals(itemId)) {
                assertEquals(PRODUCT_3_QUANTITY_2, item.getAmount());
            } else {
                fail();
            }
            assertThat(item.getOrder()).isEqualToComparingFieldByField(order);
        }
    }

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setCountryCode("7");
        user.setMobileNumber("7777777777");
        user.setActualAddress(address());
        user.setBonusAccount(bonusAccount());
        user.setUsedPromoCodes(new ArrayList<>());
        return user;
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
        return address;
    }

    private static BonusAccount bonusAccount() {
        BonusAccount account = new BonusAccount();
        account.setBonusAmount(0);
        account.setBonusesSpent(0);
        account.setRegistrationPromoCode("AAAAA");
        account.setRegistrationPromoCodeUsed(false);
        account.setRegistrationPromoCashbackReturned(false);
        return account;
    }

    private static final String QUEUE_NAME = "queue-name";

    private static final int PRODUCT_1_PRICE = 512;
    private static final String PRODUCT_1_ID = "PRODUCT_1_ID";

    private static Product product1() {
        Product product = new Product();
        product.setId(PRODUCT_1_ID);
        product.setPrice(PRODUCT_1_PRICE);
        product.setName("PRODUCT_1");
        product.setIncludedInMenu(true);
        return product;
    }

    private static final int PRODUCT_2_PRICE = 256;
    private static final String PRODUCT_2_ID = "PRODUCT_2_ID";

    private static Product product2() {
        Product product = new Product();
        product.setId(PRODUCT_2_ID);
        product.setPrice(PRODUCT_2_PRICE);
        product.setName("PRODUCT_2");
        product.setIncludedInMenu(true);
        return product;
    }

    private static final int PRODUCT_3_PRICE = 128;
    private static final String PRODUCT_3_ID = "PRODUCT_3_ID";

    private static Product product3() {
        Product product = new Product();
        product.setId(PRODUCT_3_ID);
        product.setPrice(PRODUCT_3_PRICE);
        product.setName("PRODUCT_3");
        product.setIncludedInMenu(true);
        return product;
    }

    private static OrderRegistrationDto registrationDto1() {
        OrderRegistrationDto dto = new OrderRegistrationDto();
        dto.setOrderItems(List.of(item1(), item2(), item3()));
        dto.setTotalCost(totalCostQuantity1());
        dto.setCutlery(true);
        return dto;
    }

    private static final Integer PRODUCT_1_QUANTITY_1 = 1;
    private static final Integer PRODUCT_2_QUANTITY_1 = 3;
    private static final Integer PRODUCT_3_QUANTITY_1 = 2;

    private static OrderRegistrationItemDto item1() {
        OrderRegistrationItemDto item = new OrderRegistrationItemDto();
        item.setAmount(PRODUCT_1_QUANTITY_1);
        item.setProductId(PRODUCT_1_ID);
        return item;
    }

    private static OrderRegistrationItemDto item2() {
        OrderRegistrationItemDto item = new OrderRegistrationItemDto();
        item.setAmount(PRODUCT_2_QUANTITY_1);
        item.setProductId(PRODUCT_2_ID);
        return item;
    }

    private static OrderRegistrationItemDto item3() {
        OrderRegistrationItemDto item = new OrderRegistrationItemDto();
        item.setAmount(PRODUCT_3_QUANTITY_1);
        item.setProductId(PRODUCT_3_ID);
        return item;
    }

    private static Map<Product, Integer> productQuantity1() {
        return Map.of(product1(), PRODUCT_1_QUANTITY_1, product2(), PRODUCT_2_QUANTITY_1, product3(), PRODUCT_3_QUANTITY_1);
    }

    private static Integer totalCostQuantity1() {
        return productQuantity1().entrySet().stream().mapToInt(p -> p.getKey().getPrice() * p.getValue()).sum();
    }

    private static OrderRegistrationDto registrationDto2() {
        OrderRegistrationDto dto = new OrderRegistrationDto();
        dto.setOrderItems(List.of(item4(), item5(), item6()));
        dto.setTotalCost(totalCostQuantity2());
        dto.setCutlery(true);
        return dto;
    }

    private static final Integer PRODUCT_1_QUANTITY_2 = 5;
    private static final Integer PRODUCT_2_QUANTITY_2 = 1;
    private static final Integer PRODUCT_3_QUANTITY_2 = 4;

    private static OrderRegistrationItemDto item4() {
        OrderRegistrationItemDto item = new OrderRegistrationItemDto();
        item.setAmount(PRODUCT_1_QUANTITY_2);
        item.setProductId(PRODUCT_1_ID);
        return item;
    }

    private static OrderRegistrationItemDto item5() {
        OrderRegistrationItemDto item = new OrderRegistrationItemDto();
        item.setAmount(PRODUCT_2_QUANTITY_2);
        item.setProductId(PRODUCT_2_ID);
        return item;
    }

    private static OrderRegistrationItemDto item6() {
        OrderRegistrationItemDto item = new OrderRegistrationItemDto();
        item.setAmount(PRODUCT_3_QUANTITY_2);
        item.setProductId(PRODUCT_3_ID);
        return item;
    }

    private static Map<Product, Integer> productQuantity2() {
        return Map.of(product1(), PRODUCT_1_QUANTITY_2, product2(), PRODUCT_2_QUANTITY_2, product3(), PRODUCT_3_QUANTITY_2);
    }

    private static Integer totalCostQuantity2() {
        return productQuantity2().entrySet().stream().mapToInt(p -> p.getKey().getPrice() * p.getValue()).sum();
    }

    private static Order order() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(NEW);
        order.setInProcessing(true);
        order.setCheckStatus(true);
        CloudPayment payment = new CloudPayment();
        payment.setPaymentCompleteRequired(false);
        order.setCloudPayment(payment);
        return order;
    }

}
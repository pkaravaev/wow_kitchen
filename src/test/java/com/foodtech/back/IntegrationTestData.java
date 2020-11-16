package com.foodtech.back;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.auth.CredentialsAuthDto;
import com.foodtech.back.dto.model.*;
import com.foodtech.back.dto.payment.PaymentDto;
import com.foodtech.back.dto.payment.cloud.BindCardDto;
import com.foodtech.back.dto.payment.cloud.CloudPaymentBaseResponse;
import com.foodtech.back.dto.payment.cloud.CloudPaymentResponse;
import com.foodtech.back.dto.payment.cloud.CloudPaymentTransactionResultDto;
import com.foodtech.back.entity.bonus.BonusAccount;
import com.foodtech.back.entity.model.*;
import com.foodtech.back.entity.model.iiko.Product;
import com.foodtech.back.entity.model.iiko.ProductCategory;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.entity.payment.PaymentType;
import com.foodtech.back.entity.payment.cloud.CloudPayment;
import com.foodtech.back.entity.payment.cloud.CloudPaymentStatus;
import com.foodtech.back.security.JwtTokenService;
import com.foodtech.back.util.StringUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.foodtech.back.util.DateUtil.toDate;

/*
* Здесь сожержатся тестовые данные аналогичные содержащимся в permanent-test-data.sql.
* Если менять какие-либо данные в одном - менять и в другом.
* */
@Component
public class IntegrationTestData {

    @Autowired
    private JwtTokenService tokenUtil;

    @Autowired
    ResourcesProperties properties;

    public static final String COUNTRY_CODE = "7";
    public static final String USER_1_MOBILE_NUMBER = "7777777777";
    public static final String USER_2_MOBILE_NUMBER = "4444444444";
    public static final String AUTH_NEW_USER_MOBILE_NUMBER = "9999999999";
    public static final String USER_2_MOBILE_NUMBER_CRYPT = "dQceMjT94+hrh9mVwgZs8A==";
    public static final String INVALID_MOBILE_NUMBER = "9999999999999999";

    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    public static final String REFRESH_TOKEN_2 = "REFRESH_TOKEN_2";
    public static final String SMS_CODE = "7777";
    public static final String USER_1_REG_PROMO_CODE = "AAAAA";
    public static final String USER_2_REG_PROMO_CODE = "BBBBB";
    public static final String IMPERSONAL_PROMO_CODE = "IMPERSONAL";
    public static final Integer IMPERSONAL_PROMO_CODE_AMOUNT = 1;

    public static final String TEST_QUEUE_NAME = "queue-name";

    public static final FullMobileNumber USER_1_FULL_MOBILE_NUMBER = new FullMobileNumber(COUNTRY_CODE, USER_1_MOBILE_NUMBER);
    public static final FullMobileNumber USER_2_FULL_MOBILE_NUMBER = new FullMobileNumber(COUNTRY_CODE, USER_2_MOBILE_NUMBER);
    public static final FullMobileNumber AUTH_NEW_USER_FULL_MOBILE_NUMBER = new FullMobileNumber(COUNTRY_CODE, AUTH_NEW_USER_MOBILE_NUMBER);
    public static final FullMobileNumber INVALID_FULL_MOBILE_NUMBER = new FullMobileNumber(COUNTRY_CODE, "777");

    public static final Long BIND_CARD_TRANSACTION_ID_USER_1 = 4444L;
    public static final Long NEED_3DS_RESPONSE_TRANSACTION_ID = 1111L;

    public static final int TEST_ORDERS_TOTAL_AMOUNT = 3;
    public static final int USER_1_CARDS_AMOUNT = 2;
    public static final int USER_2_CARDS_AMOUNT = 1;

    public static final LocalDateTime KITCHEN_OPEN_TIME = LocalDateTime.of(2019, 1, 2, 10, 0);
    public static final LocalDateTime KITCHEN_CLOSED_TIME = LocalDateTime.of(2019, 1, 1, 7, 0);

    public String user1AuthTokenHeader() {
        return "Bearer " + generateUser1AuthToken();
    }

    public String generateUser1AuthToken() {
        return tokenUtil.getAuthToken(StringUtil.formFullMobileNumberStr(USER_1_FULL_MOBILE_NUMBER));
    }

    public String user2AuthTokenHeader() {
        return "Bearer " + generateUser2AuthToken();
    }

    private String generateUser2AuthToken() {
        return tokenUtil.getAuthToken(StringUtil.formFullMobileNumberStr(USER_2_FULL_MOBILE_NUMBER));
    }

    public User user1() {
        User user = new User();
        user.setId(1L);
        user.setEnabled(true);
        user.setCountryCode(COUNTRY_CODE);
        user.setMobileNumber(USER_1_MOBILE_NUMBER);
        user.setName("TEST_USER_1");
        BonusAccount bonusAccount = new BonusAccount();
        bonusAccount.setBonusAmount(100);
        bonusAccount.setRegistrationPromoCode(USER_1_REG_PROMO_CODE);
        user.setBonusAccount(bonusAccount);
        return user;
    }

    public User user2() {
        User user = new User();
        user.setId(2L);
        user.setEnabled(true);
        user.setCountryCode(COUNTRY_CODE);
        user.setMobileNumber(USER_2_MOBILE_NUMBER);
        user.setName("TEST_USER_2");
        BonusAccount bonusAccount = new BonusAccount();
        bonusAccount.setBonusAmount(0);
        bonusAccount.setRegistrationPromoCode(USER_2_REG_PROMO_CODE);
        user.setBonusAccount(bonusAccount);
        return user;
    }

    public CredentialsAuthDto smsAuthNewUserFromFrontData() {
        CredentialsAuthDto authDtoFromFront = new CredentialsAuthDto();
        authDtoFromFront.setFullNumber(new FullMobileNumber(COUNTRY_CODE, AUTH_NEW_USER_MOBILE_NUMBER));
        authDtoFromFront.setSmsCode(SMS_CODE);
        authDtoFromFront.setAddress(addressNew());
        authDtoFromFront.setLatitude(BigDecimal.valueOf(51.132767));
        authDtoFromFront.setLongitude(BigDecimal.valueOf(71.422205));
        return authDtoFromFront;
    }

    public CredentialsAuthDto smsAuthExistingUserFromFrontData() {
        CredentialsAuthDto authDtoFromFront = new CredentialsAuthDto();
        authDtoFromFront.setFullNumber(new FullMobileNumber(COUNTRY_CODE, USER_2_MOBILE_NUMBER));
        authDtoFromFront.setSmsCode(SMS_CODE);
        authDtoFromFront.setAddress(addressNew());
        authDtoFromFront.setLatitude(BigDecimal.valueOf(51.132767));
        authDtoFromFront.setLongitude(BigDecimal.valueOf(71.422205));
        return authDtoFromFront;
    }

    public Address address1() {
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

    public Address address2() {
        Address address = new Address();
        address.setId(2L);
        address.setCity("Нур-Султан");
        address.setStreet("улица Жубанова");
        address.setHome("3");
        address.setHousing("3A");
        address.setEntrance("4");
        address.setDoorphone("51K7009");
        address.setFloor("7");
        address.setApartment("99");
        address.setComment("Вход с улицы");
        address.setActual(false);
        return address;
    }

    public Address address3() {
        Address address = new Address();
        address.setCity("Нур-Султан");
        address.setStreet("улица Жубанова");
        address.setHome("5");
        address.setHousing("7А");
        address.setEntrance("4");
        address.setDoorphone("51K7011");
        address.setFloor("7");
        address.setApartment("88");
        address.setComment("Вход с улицы");
        address.setActual(false);
        return address;
    }

    public Address addressNew() {
        Address address = new Address();
        address.setCity("Нур-Султан");
        address.setStreet("улица Жубанова");
        address.setHome("4");
        address.setHousing("4Б");
        address.setEntrance("7");
        address.setDoorphone("51K7012");
        address.setFloor("6");
        address.setApartment("44");
        address.setComment("Вход со двора");
        address.setActual(true);
        return address;
    }

    public BankCard bankCard1() {
        BankCard bankCard = new BankCard();
        bankCard.setId(1L);
        bankCard.setToken("SOME_CARD_TOKEN");
        bankCard.setCardMask("777777******9999");
        bankCard.setCardType("VISA");
        bankCard.setCardIssuer("Iron Bank of Braavos");
        bankCard.setActual(true);
        return bankCard;
    }

    public BankCard bankCard2() {
        BankCard bankCard = new BankCard();
        bankCard.setId(2L);
        bankCard.setToken("SOME_ANOTHER_CARD_TOKEN");
        bankCard.setCardMask("999999******7777");
        bankCard.setCardType("Master Card");
        bankCard.setCardIssuer("Iron Bank of Braavos");
        bankCard.setActual(false);
        return bankCard;
    }

    public BankCard bankCard3() {
        BankCard bankCard = new BankCard();
        bankCard.setId(3L);
        bankCard.setToken("ONE_MORE_CARD_TOKEN");
        bankCard.setCardMask("111111******4444");
        bankCard.setCardType("Master Card");
        bankCard.setCardIssuer("Iron Bank of Braavos");
        bankCard.setActual(false);
        return bankCard;
    }

    public UserPreferences user1PreferencesData() {
        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setId(1L);
        userPreferences.setNotSpicy(false);
        userPreferences.setVegetarian(false);
        userPreferences.setWithoutNuts(false);
        userPreferences.setDontLike(Set.of("лук", "яйцо"));
        return userPreferences;
    }

    public UserPreferences updatedUser1PreferencesData() {
        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setId(1L);
        userPreferences.setNotSpicy(true);
        userPreferences.setVegetarian(true);
        userPreferences.setWithoutNuts(true);
        userPreferences.setDontLike(Set.of("морепродукты"));
        return userPreferences;
    }

    public String generateExpiredToken(Map<String, Object> claims, String subject) {
        final LocalDateTime createdDate = LocalDateTime.now();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(toDate(createdDate))
                .setExpiration(toDate(createdDate))
                .signWith(SignatureAlgorithm.HS512, properties.getSecret())
                .compact();
    }

    private ProductCategory category1() {
        ProductCategory category1 = new ProductCategory();
        category1.setName("TEST_CATEGORY_1");
        category1.setId("1");
        category1.setViewOrder(0);
        return category1;
    }

    private ProductCategory category2() {
        ProductCategory category2 = new ProductCategory();
        category2.setName("TEST_CATEGORY_2");
        category2.setId("2");
        category2.setViewOrder(1);
        return category2;
    }

    private ProductCategory category3() {
        ProductCategory category3 = new ProductCategory();
        category3.setName("TEST_CATEGORY_3");
        category3.setId("3");
        category3.setViewOrder(2);
        return category3;
    }

    /* Включены в меню */
    public Product product1() {
        Product product1 = new Product();
        product1.setName("TEST_PRODUCT_1");
        product1.setId("1");
        product1.setIncludedInMenu(true);
        product1.setProductCategory(category1());
        product1.setPrice(100);
        return product1;
    }

    private Product product2() {
        Product product2 = new Product();
        product2.setName("TEST_PRODUCT_2");
        product2.setId("2");
        product2.setIncludedInMenu(true);
        product2.setProductCategory(category1());
        product2.setPrice(100);
        return product2;
    }

    private Product product3() {
        Product product3 = new Product();
        product3.setName("TEST_PRODUCT_3");
        product3.setId("3");
        product3.setIncludedInMenu(true);
        product3.setProductCategory(category1());
        product3.setPrice(100);
        return product3;
    }

    private Product product4() {
        Product product4 = new Product();
        product4.setName("TEST_PRODUCT_4");
        product4.setId("4");
        product4.setIncludedInMenu(true);
        product4.setProductCategory(category2());
        product4.setPrice(100);
        return product4;
    }

    private Product product5() {
        Product product5 = new Product();
        product5.setName("TEST_PRODUCT_5");
        product5.setId("5");
        product5.setIncludedInMenu(true);
        product5.setProductCategory(category2());
        product5.setPrice(100);
        return product5;
    }

    private Product product6() {
        Product product6 = new Product();
        product6.setName("TEST_PRODUCT_6");
        product6.setId("6");
        product6.setIncludedInMenu(true);
        product6.setProductCategory(category2());
        product6.setPrice(100);
        return product6;
    }

    private  Product product7() {
        Product product7 = new Product();
        product7.setName("TEST_PRODUCT_7");
        product7.setId("7");
        product7.setIncludedInMenu(true);
        product7.setProductCategory(category3());
        product7.setPrice(100);
        return product7;
    }

    private  Product product8() {
        Product product8 = new Product();
        product8.setName("TEST_PRODUCT_8");
        product8.setId("8");
        product8.setIncludedInMenu(true);
        product8.setProductCategory(category3());
        product8.setPrice(100);
        return product8;
    }

    private  Product product9() {
        Product product9 = new Product();
        product9.setName("TEST_PRODUCT_9");
        product9.setId("9");
        product9.setIncludedInMenu(true);
        product9.setProductCategory(category3());
        product9.setPrice(100);
        return product9;
    }

    /* Не включены в меню */
    private  Product product10NotIncluded() {
        Product product10 = new Product();
        product10.setName("TEST_PRODUCT_10");
        product10.setId("10");
        product10.setIncludedInMenu(false);
        product10.setProductCategory(category3());
        product10.setPrice(100);
        return product10;
    }

    private  Product product11InStopList() {
        Product product11 = new Product();
        product11.setName("TEST_PRODUCT_11");
        product11.setId("11");
        product11.setIncludedInMenu(true);
        product11.setInStopList(true);
        product11.setProductCategory(category3());
        product11.setPrice(100);
        return product11;
    }

    public List<Product> testProductIncludedData() {
        return List.of(product1(), product2(), product3(), product4(), product5(), product6(),
                product7(), product8(), product9());
    }

    public List<Product> testProductNotIncludedData() {
        return List.of(product10NotIncluded(), product11InStopList());
    }

    public Order order1() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user1());
        order.setAddress(address1());
        order.setBankCard(bankCard1());
        order.setTotalCost(200);
        order.setStatus(OrderStatus.PAID);
        order.setCreated(LocalDateTime.of(2019, 1, 1, 0, 0));
        order.setAppliedBonusAmount(0);
        order.setProductsCost(200);
        order.setIikoOrderId("IIKO_ORDER_ID");
        order.setCloudPayment(new CloudPayment());
        order.getCloudPayment().setTransactionId(333777L);
        order.getCloudPayment().setCloudPaymentStatus(CloudPaymentStatus.Authorized);
        order.getCloudPayment().setDeclineReason("Approved");
        order.getCloudPayment().setDeclineReasonCode(0);
        order.setPaymentType(PaymentType.CARD);
        order.setStatusQueueName("queue-name");
        order.setPaidTime(LocalDateTime.of(2019, 1, 1, 0, 0));
        order.setDeliveryTime(30);

        OrderItem item1 = new OrderItem();
        item1.setOrder(order);
        item1.setProduct(product1());
        item1.setAmount(1);
        OrderItem item2 = new OrderItem();
        item2.setOrder(order);
        item2.setProduct(product2());
        item2.setAmount(1);

        order.setItems(List.of(item1, item2));

        return order;
    }

    public OrderInfoDto order1InfoDto() {
        OrderInfoDto orderInfoDto = new OrderInfoDto();
        orderInfoDto.setId(1L);
        orderInfoDto.setTotalCost(200);
        orderInfoDto.setOrderStatus(OrderStatus.PAID);
        orderInfoDto.setAppliedBonusAmount(0);
        orderInfoDto.setPaymentType(PaymentType.CARD.toString());
        orderInfoDto.setCreated(LocalDateTime.of(2019, 1, 1, 0, 0));
        return orderInfoDto;
    }

    public OrderItemDto order1_ItemDto1() {
        OrderItemDto itemDto1 = new OrderItemDto();
        itemDto1.setProduct(product1());
        itemDto1.setAmount(1);
        return itemDto1;
    }

    public OrderItemDto order1_ItemDto2() {
        OrderItemDto itemDto2 = new OrderItemDto();
        itemDto2.setProduct(product2());
        itemDto2.setAmount(1);
        return itemDto2;
    }

    public OrderInfoDto order2InfoDto() {
        OrderInfoDto orderInfoDto = new OrderInfoDto();
        orderInfoDto.setId(2L);
        orderInfoDto.setTotalCost(300);
        orderInfoDto.setOrderStatus(OrderStatus.PAID);
        orderInfoDto.setAppliedBonusAmount(0);
        orderInfoDto.setPaymentType(PaymentType.CARD.toString());
        orderInfoDto.setCreated(LocalDateTime.of(2019, 2, 2, 0, 0));
        return orderInfoDto;
    }

    public OrderItemDto order2_ItemDto3() {
        OrderItemDto itemDto3 = new OrderItemDto();
        itemDto3.setProduct(product3());
        itemDto3.setAmount(1);
        return itemDto3;
    }

    public OrderItemDto Order2_ItemDto4() {
        OrderItemDto itemDto4 = new OrderItemDto();
        itemDto4.setProduct(product4());
        itemDto4.setAmount(1);
        return itemDto4;
    }

    public OrderItemDto order2_ItemDto5() {
        OrderItemDto itemDto5 = new OrderItemDto();
        itemDto5.setProduct(product5());
        itemDto5.setAmount(1);
        return itemDto5;
    }

    public OrderRegistrationDto orderRegistrationDto() {
        OrderRegistrationDto registrationDto = new OrderRegistrationDto();
        registrationDto.setOrderItems(List.of(registrationItemDto1(), registrationItemDto2()));
        registrationDto.setTotalCost(400);
        registrationDto.setCutlery(true);
        return registrationDto;
    }

    public OrderRegistrationItemDto registrationItemDto1() {
        OrderRegistrationItemDto itemDto = new OrderRegistrationItemDto();
        itemDto.setAmount(3);
        itemDto.setProductId(product1().getId());
        return itemDto;
    }

    public OrderRegistrationItemDto registrationItemDto2() {
        OrderRegistrationItemDto itemDto = new OrderRegistrationItemDto();
        itemDto.setAmount(2);
        itemDto.setProductId(product2().getId());
        return itemDto;
    }

    public OrderInfoDto orderRegistrationResponseDto() {
        OrderInfoDto dto = new OrderInfoDto();
        dto.setStatusQueueName(TEST_QUEUE_NAME);
        dto.setTotalDeliveryTime(30);
        return dto;
    }

    public PaymentDto order4_PaymentDto() {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setOrderId(4L);
        return paymentDto;
    }

    public BindCardDto bindCardDto() {
        BindCardDto dto = new BindCardDto();
        dto.setCardCryptogramPacket("CRYPTOGRAM_PACKET");
        return dto;
    }

    public CloudPaymentBaseResponse need3DSResponse() {
        CloudPaymentBaseResponse cloudResponse = new CloudPaymentBaseResponse();
        cloudResponse.setSuccess(false);
        CloudPaymentResponse cloudModel = new CloudPaymentResponse();
        cloudModel.setTransactionId(NEED_3DS_RESPONSE_TRANSACTION_ID);
        cloudModel.setPaReq("PaReq");
        cloudModel.setAcsUrl("https://acs.ru");
        cloudResponse.setModel(cloudModel);
        return cloudResponse;
    }

    public CloudPaymentTransactionResultDto need3DSTransactionResult() {
        return CloudPaymentTransactionResultDto
                .builder()
                .need3DS(true)
                .merchantData(String.valueOf(NEED_3DS_RESPONSE_TRANSACTION_ID))
                .acsUrl("https://acs.ru")
                .paReq("PaReq")
                .termUrl(properties.getCardBinding3DSCallbackUrl())
                .amqpHost(properties.getRabbitMqHost())
                .queueName(properties.getRabbitMqCardBindingQueuePrefix() + DigestUtils.md5Hex(String.valueOf(NEED_3DS_RESPONSE_TRANSACTION_ID)))
                .build();
    }

    public CloudPaymentBaseResponse transactionApprovedResponse() {
        CloudPaymentBaseResponse cloudResponse = new CloudPaymentBaseResponse();
        cloudResponse.setSuccess(true);
        CloudPaymentResponse cloudModel = new CloudPaymentResponse();
        cloudModel.setCardType("VISA");
        cloudModel.setCardFirstSix("666666");
        cloudModel.setCardLastFour("4444");
        cloudModel.setTransactionId(BIND_CARD_TRANSACTION_ID_USER_1);
        cloudModel.setReasonCode(0);
        cloudModel.setReason("Approved");
        cloudModel.setToken("CARD_TOKEN");
        cloudModel.setStatus(CloudPaymentStatus.Authorized);
        cloudResponse.setModel(cloudModel);
        return cloudResponse;
    }

    public CloudPaymentBaseResponse transactionDeclinedResponse() {
        CloudPaymentBaseResponse cloudResponse = new CloudPaymentBaseResponse();
        cloudResponse.setSuccess(false);
        CloudPaymentResponse cloudModel = new CloudPaymentResponse();
        cloudModel.setTransactionId(BIND_CARD_TRANSACTION_ID_USER_1);
        cloudModel.setStatus(CloudPaymentStatus.Declined);
        cloudModel.setReason("InsufficientFunds");
        cloudModel.setReasonCode(5);
        cloudModel.setCardHolderMessage("Недостаточно средств на карте");
        cloudResponse.setModel(cloudModel);
        return cloudResponse;
    }

//    public DeliveryZone getTestDeliveryZone() {
//        DeliveryZone deliveryZone = new DeliveryZone();
//        deliveryZone.setCoordinates(new ArrayList<>());
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1460117), BigDecimal.valueOf(71.411814)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1492423), BigDecimal.valueOf(71.4124148)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1531187), BigDecimal.valueOf(71.4125006)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1571024), BigDecimal.valueOf(71.411299)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1544646), BigDecimal.valueOf(71.4040034)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1536032), BigDecimal.valueOf(71.3985102)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1510728), BigDecimal.valueOf(71.3918154)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1497807), BigDecimal.valueOf(71.3923304)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1482193), BigDecimal.valueOf(71.3860648)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1266774), BigDecimal.valueOf(71.4044325)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1176268), BigDecimal.valueOf(71.401171)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1123464), BigDecimal.valueOf(71.4387648)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1210748), BigDecimal.valueOf(71.4418547)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1207516), BigDecimal.valueOf(71.4449446)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1227449), BigDecimal.valueOf(71.4455454)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1230681), BigDecimal.valueOf(71.4491503)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1248458), BigDecimal.valueOf(71.4506953)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1270544), BigDecimal.valueOf(71.4505236)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1286704), BigDecimal.valueOf(71.447777)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1311481), BigDecimal.valueOf(71.4463179)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1333564), BigDecimal.valueOf(71.4343874)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1365879), BigDecimal.valueOf(71.4356749)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.143804), BigDecimal.valueOf(71.4322417)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1488116), BigDecimal.valueOf(71.43207)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1512344), BigDecimal.valueOf(71.427521)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1470886), BigDecimal.valueOf(71.4226286)));
//        deliveryZone.getCoordinates().add(new DeliveryCoordinate(BigDecimal.valueOf(51.1460117), BigDecimal.valueOf(71.411814)));
//
//        return deliveryZone;
//    }


    public static BigDecimal LATITUDE = BigDecimal.valueOf(51.132767);
    public static BigDecimal LONGITUDE = BigDecimal.valueOf(71.422205);

}

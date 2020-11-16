package com.foodtech.back.dto.payment.cloud;

import com.foodtech.back.entity.payment.cloud.CloudPaymentStatus;
import lombok.Data;

@Data
public class CloudPaymentResponse {

    private Long transactionId;

    private String invoiceId; /* наш номер заказа */

    private String accountId; /* ID юзера */

    private String cardFirstSix; /* первые 6 цифр банк. карты */

    private String cardLastFour; /* последние 4 цифры банк. карты */

    private String cardExpDate;

    private String cardType;

    private String issuer; /* банк, выпустивший карту */

    private CloudPaymentStatus status;

    private Integer statusCode;

    private String reason; /* причина отказа */

    private Integer reasonCode;

    private String cardHolderMessage;

    private String token;

    private String name;


    /* параметры 3DS */
    private String paReq;

    private String acsUrl;

    /* Все возможные поля ответа:
    * "PaymentAmount": 10.00000,
        "PaymentCurrency": "KZT",
        "PaymentCurrencyCode": 0,
        "InvoiceId": "1234567",
        "AccountId": "user_x",
        "Email": null,
        "Description": "Оплата товаров в example.com",
        "JsonData": null,
        "CreatedDate": "\/Date(1401718880000)\/",
        "CreatedDateIso":"2014-08-09T11:49:41", //все даты в UTC
        "TestMode": true,
        "IpAddress": "195.91.194.13",
        "IpCountry": "RU",
        "IpCity": "Уфа",
        "IpRegion": "Республика Башкортостан",
        "IpDistrict": "Приволжский федеральный округ",
        "IpLatitude": 54.7355,
        "IpLongitude": 55.991982,
        "CardFirstSix": "411111",
        "CardLastFour": "1111",
        "CardExpDate": "05/19",
        "CardType": "Visa",
        "CardTypeCode": 0,
        "Issuer": "Sberbank of Russia",
        "IssuerBankCountry": "RU",
        "Status": "Declined",
        "StatusCode": 5,
        "Reason": "InsufficientFunds", // причина отказа
        "ReasonCode": 5051, //код отказа
        "CardHolderMessage":"Недостаточно средств на карте", //сообщение для покупателя
        "Name": "CARDHOLDER NAME",*/
}

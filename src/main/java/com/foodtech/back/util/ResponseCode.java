package com.foodtech.back.util;

import lombok.Getter;

@Getter
public enum ResponseCode {

    OK("0000"),
    AUTHORIZED("0001"),
    SEND_SMS("0002"),
    SMS_SENDING_FAIL("0003"),
    SMS_DELAY("0004"),
    SMS_NOT_FOUND("0005"),
    SMS_INVALID("0006"),
    SMS_MAX_ATTEMPT("0007"),
    SMS_EXPIRED("0008"),
    MOB_NUM_INVALID("0009"),
    NEW_USER("0010"),
    ERROR_CREATING_USER("0011"),
    TOKEN_INVALID("0012"),
    TOKEN_EXPIRED("0013"),
    REQUEST_INVALID("0014"),
    USER_NOT_FOUND("0015"),
    UNAUTHORIZED("0016"),
    REFRESH_TOKEN_DATA_INVALID("0017"),
    ORDER_SAVE_FAILED("0018"),
    PAYMENT_FAILED("0019"),
    CART_INVALID("0020"),
    CARD_BINDING_FAILED("0021"),
    NO_BANK_CARD_FOUND("0022"),
    ORDER_NOT_FOUND("0023"),
    PROMO_CODE_NOT_FOUND("0024"),
    REGISTRATION_PROMO_CODE_USED("0025"),
    REGISTRATION_PROMO_CODE_EXISTS("0026"),
    COORDINATES_NOT_IN_DELIVERY_ZONE("0027"),
    USER_ADDRESS_INVALID("0028"),
    OWN_REG_PROMO_CODE("0029"),
    PAYMENT_TRANSACTION_DECLINED("0030"),
    REQUEST_SENDING_FAILED("0031"),
    KITCHEN_CLOSED("0032"),
    PROMO_CODE_USED("0033"),
    DELIVERY_ZONE_NOT_ACTIVE("0034");

    private String code;

    ResponseCode(String code) {
        this.code = code;
    }
}
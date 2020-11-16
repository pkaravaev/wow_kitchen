package com.foodtech.back.util.exceptions;

import lombok.Getter;

@Getter
public class PaymentRequestInvalidException extends RuntimeException {
    public PaymentRequestInvalidException(String message) {
        super(message);
    }
}

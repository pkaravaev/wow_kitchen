package com.foodtech.back.util.exceptions;

public class CardBindingRequestInvalidException extends RuntimeException {

    public CardBindingRequestInvalidException(String exceptionMsg) {
        super(exceptionMsg);
    }
}

package com.foodtech.back.util.exceptions;

import lombok.Getter;

@Getter
public class CartInvalidException extends RuntimeException {

    public CartInvalidException(String exceptionMsg) {
        super(exceptionMsg);
    }
}

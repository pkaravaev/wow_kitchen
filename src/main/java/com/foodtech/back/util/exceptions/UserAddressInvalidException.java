package com.foodtech.back.util.exceptions;

public class UserAddressInvalidException extends AbstractAppException {

    public UserAddressInvalidException(String userNumber) {
        super(userNumber);
    }
}

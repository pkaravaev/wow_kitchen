package com.foodtech.back.util.exceptions;

public class MobileNumberInvalidException extends AbstractAppException {

    public MobileNumberInvalidException(String userNumber) {
        super(userNumber);
    }
}

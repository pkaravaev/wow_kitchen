package com.foodtech.back.util.exceptions;

public class SmsNotFoundException extends AbstractAppException {

    public SmsNotFoundException(String userNumber) {
        super(userNumber);
    }
}

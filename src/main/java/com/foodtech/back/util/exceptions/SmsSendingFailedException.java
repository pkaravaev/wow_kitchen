package com.foodtech.back.util.exceptions;

public class SmsSendingFailedException extends AbstractAppException {

    public SmsSendingFailedException(String userNumber) {
        super(userNumber);
    }
}

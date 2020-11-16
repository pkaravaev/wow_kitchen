package com.foodtech.back.util.exceptions;

public class SmsSendingNotAllowedException extends AbstractAppException {

    public SmsSendingNotAllowedException(String userNumber) {
        super(userNumber);
    }
}

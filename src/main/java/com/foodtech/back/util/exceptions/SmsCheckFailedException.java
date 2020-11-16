package com.foodtech.back.util.exceptions;

import com.foodtech.back.util.ResponseCode;
import lombok.Getter;

@Getter
public class SmsCheckFailedException extends AbstractAppException {

    private ResponseCode reason;

    private String userMessage;

    public SmsCheckFailedException(String userNumber, ResponseCode reason, String userMessage) {
        super(userNumber);
        this.reason = reason;
        this.userMessage = userMessage;
    }
}

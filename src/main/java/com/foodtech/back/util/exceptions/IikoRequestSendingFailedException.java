package com.foodtech.back.util.exceptions;

import lombok.Getter;

@Getter
public class IikoRequestSendingFailedException extends AbstractAppException {

    public IikoRequestSendingFailedException(String userNumber, String userMsg) {
        super(userNumber);
    }
}

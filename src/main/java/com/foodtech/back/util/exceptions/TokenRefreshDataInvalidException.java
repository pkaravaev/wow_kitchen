package com.foodtech.back.util.exceptions;

public class TokenRefreshDataInvalidException extends AbstractAppException {

    public TokenRefreshDataInvalidException(String userNumber) {
        super(userNumber);
    }
}

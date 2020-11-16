package com.foodtech.back.util.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
abstract class AbstractAppException extends RuntimeException {

    private String userNumber;

    AbstractAppException(String userNumber) {
        this.userNumber = userNumber;
    }
}

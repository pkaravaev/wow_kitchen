package com.foodtech.back.dto.auth;

import com.foodtech.back.util.ResponseCode;
import lombok.Data;

@Data
public class SmsCheckResultDto {

    private ResponseCode responseCode;

    private String userMsg;

    public SmsCheckResultDto(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public SmsCheckResultDto(ResponseCode responseCode, String userMsg) {
        this.responseCode = responseCode;
        this.userMsg = userMsg;
    }
}

package com.foodtech.back.service.notification.sms;

import lombok.Data;

@Data
public class SmsMessageDto {

    private String country;
    private String mobNumber;
    private String content;
    private String title;
    private String template;
    private String innerError;
    private Boolean success;
    private String response;
    private String senderType;

    public SmsMessageDto() {
    }

    public SmsMessageDto(Boolean success) {
        this.success = success;
    }

    public SmsMessageDto(String mobNumber, String content, String country) {
        this.mobNumber = mobNumber;
        this.content = content;
        this.country = country;
    }

    public static SmsMessageDto create(String to, String content, String country) {
        SmsMessageDto dto = new SmsMessageDto();
        dto.mobNumber = to;
        dto.content = content;
        dto.country = country;
        return dto;
    }
}

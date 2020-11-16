package com.foodtech.back.service.notification.sms;

import lombok.Data;

@Data
public class ReportLog {

    private Long id;
    private String dateTime;
    private Boolean success;
    private String response;
    private String to;
    private String messageType;
    private String senderType;
    private String title;
    private String content;
}

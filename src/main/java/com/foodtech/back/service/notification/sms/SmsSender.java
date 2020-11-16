package com.foodtech.back.service.notification.sms;

public interface SmsSender {

    SmsMessageDto send(SmsMessageDto dto);
}

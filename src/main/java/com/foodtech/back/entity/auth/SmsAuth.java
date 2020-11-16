package com.foodtech.back.entity.auth;

import com.foodtech.back.entity.AbstractIdEntity;
import com.foodtech.back.util.converter.JpaCryptoConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_sms_auth")
@Data
@EqualsAndHashCode(exclude = {"smsCode", "attempt", "lastSend", "used"}, callSuper = false)
public class SmsAuth extends AbstractIdEntity {

    private String countryCode;

    @Convert(converter = JpaCryptoConverter.class)
    private String mobileNumber;

    private String smsCode;

    private Integer attempt;

    private LocalDateTime lastSend;

    private Boolean used;
}

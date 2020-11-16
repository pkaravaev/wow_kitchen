package com.foodtech.back.dto.payment.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodtech.back.entity.payment.BankCard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CloudPaymentTransactionResultDto {

    @JsonProperty("needThreeDs")
    private boolean need3DS;

    private String acsUrl; /* URL адрес сервера ACS */

    @JsonProperty("PaReq")
    private String paReq;

    @JsonProperty("TermUrl")
    private String termUrl;

    @JsonProperty("MD")
    private String merchantData; /* для связки отправляемого на сервер ACS запроса и получаемого от него ответа */

    private String amqpHost;

    private String queueName;

    private BankCard bankCard;

}

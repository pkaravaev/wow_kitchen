package com.foodtech.back.dto.payment.cloud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CloudPaymentRequest {

    @JsonIgnore
    private CloudPaymentRequestType type;

    private Long transactionId;

    private String cardCryptogramPacket; /* криптограмма банк. карты, получаемая с помощью SDK моб. приложений*/

    private String token; /* токен привязанной карты */

    private String invoiceId; /* соответствует нашему Order ID */

    private String accountId; /* соответствует нашему User ID*/

    private Integer amount;

    private String currency;

    private String name;

    private String description;

    private String email;

    private String paRes; /* Рез-тат прохождения процедуры 3DS от банка-эмитента банк. карты*/

    private String jsonData; /* произвольные данные */

}

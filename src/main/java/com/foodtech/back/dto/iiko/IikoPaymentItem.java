package com.foodtech.back.dto.iiko;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class IikoPaymentItem {

    private int sum; // Сумма к оплате

    private IikoPaymentType paymentType;

    @JsonProperty(value = "isProcessedExternally")
    private boolean isProcessedExternally; // Является ли позиция оплаты проведенной

    @JsonProperty(value = "isPreliminary")
    private boolean isPreliminary; // Является ли позиция оплаты предварительной

    @JsonProperty(value = "isExternal")
    private boolean isExternal; // Принята ли позиция оплаты извне

    private String additionalData;

    IikoPaymentItem(IikoPaymentType paymentType) {
        this.isProcessedExternally = true;
        this.isPreliminary = false;
        this.isExternal = true;
        this.paymentType = paymentType;
    }
}

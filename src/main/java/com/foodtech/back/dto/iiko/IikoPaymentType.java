package com.foodtech.back.dto.iiko;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IikoPaymentType {

    String id;
    String code;
    String name;
    String comment;
    long externalRevision;
    boolean combinable;
    boolean deleted;
}

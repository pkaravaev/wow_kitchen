package com.foodtech.back.dto.iiko;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IikoCustomer {

    private String id;

    private String name;

    private String phone;

}

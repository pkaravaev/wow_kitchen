package com.foodtech.back.dto.admin;

import lombok.Data;

import java.util.List;

@Data
public class XlsAddressDto {

    private String street;
    private List<String> houses;
}

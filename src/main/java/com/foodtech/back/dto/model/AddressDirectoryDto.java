package com.foodtech.back.dto.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AddressDirectoryDto {

    private String street;

    private Set<HouseDirectoryDto> houses;
}

package com.foodtech.back.dto.iiko;

import com.foodtech.back.entity.model.iiko.IikoCityDirectory;
import com.foodtech.back.entity.model.iiko.IikoStreetDirectory;
import lombok.Data;

import java.util.List;

@Data
public class IikoCityStreetsDirectoryDto {

    private IikoCityDirectory city;

    private List<IikoStreetDirectory> streets;
}

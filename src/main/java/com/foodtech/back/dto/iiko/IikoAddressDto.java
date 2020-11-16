package com.foodtech.back.dto.iiko;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IikoAddressDto {

    private String city;

    private String street;

    private String streetId; // Идентификатор улицы (если справочник улиц синхронизирован со справочником улиц в RMS)

    private String home;

    private String housing; // Корпус

    private String apartment; // Квартира

    private String entrance; // Подъезд

    private String floor;

    private String doorphone;

    private String comment;

    private String externalCartographyId;

}

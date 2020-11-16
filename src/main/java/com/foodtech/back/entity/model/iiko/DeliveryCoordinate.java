package com.foodtech.back.entity.model.iiko;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@Access(AccessType.FIELD)
@EqualsAndHashCode(exclude = {"locationOrder"})
public class DeliveryCoordinate {

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer locationOrder;

    private DeliveryCoordinate(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static DeliveryCoordinate of(BigDecimal latitude, BigDecimal longitude) {
        return new DeliveryCoordinate(latitude, longitude);
    }

    @JsonIgnore
    public double getLatitudeDouble() {
        return latitude.doubleValue();
    }

    @JsonIgnore
    public double getLongitudeDouble() {
        return longitude.doubleValue();
    }
}

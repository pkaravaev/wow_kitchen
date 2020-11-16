package com.foodtech.back.entity.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "tb_opening_hours")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class OpeningHours {

    @Id
    private Long id;

    private Integer dayOfWeek;

    private LocalTime fromTime;

    private LocalTime toTime;

    private boolean allDay;

    private boolean closed;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private KitchenDeliveryTerminal deliveryTerminal;

    public boolean isOpenNow(LocalTime currentTime) {
        if (isAllDay()) {
            return true;
        }

        if (isClosed()) {
            return false;
        }

        return currentTime.isAfter(getFromTime()) && currentTime.isBefore(getToTime());
    }
}

package com.foodtech.back.util;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class DateUtil {

    public static final ZoneId ZONE_ID_DEFAULT = ZoneId.systemDefault();

    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZONE_ID_DEFAULT);
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZONE_ID_DEFAULT).toInstant());
    }

    public LocalDateTime getCurrentDayForTimeZone(String timeZone) {
        return LocalDateTime.now(ZoneId.of(timeZone));
    }
}

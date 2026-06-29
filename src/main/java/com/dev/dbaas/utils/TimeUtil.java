package com.dev.dbaas.utils;

import org.apache.log4j.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final Logger LOGGER = Logger.getLogger(TimeUtil.class);

    public static LocalDateTime millsToLocalDateTime(long seconds) {

        Instant instant = Instant.ofEpochSecond(seconds);
        LocalDateTime date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return date;
    }

    public static long millsToLocalDateTime(LocalDateTime time) {

        if (time == null) {
            return 0;
        }
        return time.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public static LocalDateTime parse(String dateTime, String format) {
        try {
            if(dateTime == null || dateTime.isEmpty()){
                return null;
            }
            LocalDateTime convertDate = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(format));
            return convertDate;
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
        return null;
    }
}

package com.ctel.dbaas.utils;

import lombok.extern.log4j.Log4j2;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Log4j2
public class DateUtils {

    public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyyMMdd = "yyyyMMdd";

    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";

    public static String toString(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    public static LocalDateTime convertToLocalDateTime(String dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateTime, formatter);
    }

    public static LocalDateTime convertToUTC(LocalDateTime localDateTime, String gmtOffset) {
        DateTimeFormatter gmtFormatter = DateTimeFormatter.ofPattern("O");
        ZoneOffset zoneOffset = ZoneOffset.from(gmtFormatter.parse(gmtOffset));
        OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, zoneOffset);
        OffsetDateTime utcDateTime = offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC);
        return utcDateTime.toLocalDateTime();
    }

    public static LocalDateTime convertToTimeZone(LocalDateTime localDateTime, String timeZone) {
        ZoneId zoneId = ZoneId.of(timeZone);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC).withZoneSameInstant(zoneId);

        return zonedDateTime.toLocalDateTime();
    }

    public static Date toDate(String dateString, String dateFormat) {
        if (dateString == null) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            return sdf.parse(dateString);
        } catch (Exception e) {
            log.error("ERROR convertToDate => message[{}]", e.getMessage());
            return null;
        }
    }

    public static LocalDateTime toLocalDateTime(String input, boolean isEndOfDay) {
        try {
            LocalDate date = LocalDate.parse(input);
            if (isEndOfDay) {
                return date.atTime(23, 59, 59);
            }
            return date.atStartOfDay();
        } catch (Exception e) {
            log.error("ERROR convertToLocalDateTime => message[{}]", e.getMessage());
            return null;
        }
    }

}

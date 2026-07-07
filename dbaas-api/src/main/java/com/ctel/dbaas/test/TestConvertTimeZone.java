package com.ctel.dbaas.test;

import com.ctel.dbaas.utils.DateUtils;
import lombok.SneakyThrows;

import java.time.LocalDateTime;

public class TestConvertTimeZone {

    @SneakyThrows
    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("now : " + now);

        LocalDateTime converted = DateUtils.convertToUTC(now, "GMT+07:00");
        System.out.println("UTC : " + converted.toString());
    }

}

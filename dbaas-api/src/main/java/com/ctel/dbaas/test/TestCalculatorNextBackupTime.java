package com.ctel.dbaas.test;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.utils.CommonUtils;
import com.ctel.dbaas.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TestCalculatorNextBackupTime {

    public static void main(String[] args) {

        int hour = 11;
        int minute = 46;
        int second = 0;
        Integer intervalNum = 1;
        String timeZone = "GMT+07:00";

        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime timeTriggerBackup = DateUtils.convertToUTC(LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute, second)), timeZone);

        LocalDateTime nextBackupTime;
        LocalDateTime backupTime;

        if (intervalNum == null) {
            intervalNum = 1;
        }

        String intervalType = CommonUtils.calculateIntervalType(timeTriggerBackup.getHour(), timeTriggerBackup.getMinute(), timeTriggerBackup.getSecond());
        if (intervalType.equals("DAY")) {
            backupTime = currentDateTime.withHour(timeTriggerBackup.getHour()).withMinute(timeTriggerBackup.getMinute()).withSecond(timeTriggerBackup.getSecond()).withNano(0);
            System.out.print("Hour backup : " + timeTriggerBackup.getHour() + " - ");
            System.out.println("Hour current : " + currentDateTime.getHour());

            System.out.print("Minute backup : " + timeTriggerBackup.getMinute() + " - ");
            System.out.println("Minute current : " + currentDateTime.getMinute());

            LocalTime timeBackup = LocalTime.of(timeTriggerBackup.getHour(), timeTriggerBackup.getMinute(), 0);
            LocalTime timeCurrent = LocalTime.of(currentDateTime.getHour(), currentDateTime.getMinute(), 0);

            if (timeCurrent.isBefore(timeBackup) || timeBackup.equals(timeCurrent)) {
                System.out.println("Backup in day");
                nextBackupTime = currentDateTime.withHour(timeTriggerBackup.getHour())
                        .withMinute(minute)
                        .withSecond(second)
                        .withNano(Constant.MAX_NANO_SECOND);
            } else {
                System.out.println("Backup in next " + intervalNum + " day");
                nextBackupTime = backupTime.plusDays(intervalNum);
            }

        } else {
            throw new AppException(new ErrorResponse("intervalType invalid"));
        }

        System.out.println(nextBackupTime);
        System.out.println(DateUtils.convertToTimeZone(nextBackupTime, "GMT+07:00"));
    }

}

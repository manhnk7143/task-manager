package com.ctel.dbaas.test;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.utils.CommonUtils;

import java.time.LocalDateTime;

public class TestTime {

    private static LocalDateTime calculateNextBackupTime(Integer hour, Integer minute, Integer second, Integer intervalNum) {
        String intervalType = CommonUtils.calculateIntervalType(hour, minute, second);
        return calculateNextBackupTime(hour, minute, second, intervalType, intervalNum);
    }

    private boolean checkToInitBackupNow(LocalDateTime currentTime, LocalDateTime nextBackupTime) {
        LocalDateTime currentTimeWithoutSecond = currentTime.withSecond(0).withNano(0);
        LocalDateTime nextBackupTimeWithoutSecond = nextBackupTime.withSecond(0).withNano(0);
        return currentTimeWithoutSecond.isEqual(nextBackupTimeWithoutSecond);
    }

    private static LocalDateTime calculateNextBackupTime(Integer hour, Integer minute, Integer second, String intervalType, Integer intervalNum) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime nextBackupTime = null;
        LocalDateTime backupTime;

        if (intervalNum == null) {
            intervalNum = 1;
        }

        switch (intervalType) {
            case "DAY" -> {
                backupTime = currentTime.withHour(hour).withMinute(minute).withSecond(second).withNano(0);
                if (hour >= currentTime.getHour()) {
                    if (minute >= currentTime.getMinute()) {
                        // thời gian backup trong ngày
                        nextBackupTime = currentTime.withHour(hour).withMinute(minute).withSecond(second).withNano(Constant.MAX_NANO_SECOND);
                    } else {
                        // + 1 ngày tiếp theo
                        nextBackupTime = backupTime.plusDays(intervalNum);
                    }
                } else {
                    // + 1 ngày tiếp theo
                    nextBackupTime = backupTime.plusDays(intervalNum);
                }
            }
            case "HOUR" -> {
                nextBackupTime = currentTime.withMinute(minute).withSecond(second).withNano(0);
                backupTime = currentTime.withMinute(minute).withSecond(second).withNano(0);
                LocalDateTime backupTimeMaxNanoSecond = currentTime.withSecond(second).withNano(Constant.MAX_NANO_SECOND);
                if (backupTime.isBefore(backupTimeMaxNanoSecond)) {
                    nextBackupTime = backupTime.plusHours(intervalNum);
                }
            }
            case "MINUTE" -> {
                nextBackupTime = currentTime.withSecond(second).withNano(0);
                backupTime = currentTime.withSecond(second).withNano(0);
                LocalDateTime currentTimeWithoutNanoSecond = currentTime.withSecond(second).withNano(Constant.MAX_NANO_SECOND);
                if (backupTime.isBefore(currentTimeWithoutNanoSecond)) {
                    nextBackupTime = backupTime.plusMinutes(intervalNum);
                }
            }
        }

        return nextBackupTime;
    }

    public static void main(String[] args) {
//        int hour = 7;
//        int minutes = 50;
//        int second = 0;
//        int intervalNum = 1;
//        LocalDateTime nextBackupTime = calculateNextBackupTime(hour, minutes, second, intervalNum);
//        System.out.println(nextBackupTime);
        int a = 100;
        System.out.println(-a--);
    }

}

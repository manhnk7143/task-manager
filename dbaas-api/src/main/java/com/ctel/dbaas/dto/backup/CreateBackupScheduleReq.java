package com.ctel.dbaas.dto.backup;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.utils.CommonUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreateBackupScheduleReq {

    private String instanceId;

    private Integer hour;

    private Integer minute;

    private Integer second;

    private Integer interval;

    private Integer keepRecordBackup;

    private String intervalType;

    private String timeZone;

    public void validate() {
        if (!Constant.TIME_ZONE_SUPPORT.contains(this.timeZone)) {
            throw new AppException(new ErrorResponse("time zone not support"));
        }

        if (StringUtils.isBlank(instanceId)) {
            throw new AppException(new ErrorResponse("instanceId cannot be blank"));
        }

        if (this.hour == null) {
            throw new AppException(new ErrorResponse("hour is required"));
        }

        if (0 > this.hour || this.hour > 23) {
            throw new AppException(new ErrorResponse("The value of the hour must be in the range 0 - 23"));
        }

        if (this.minute != null && (0 > this.minute || this.minute > 59)) {
            throw new AppException(new ErrorResponse("The value of the minute must be in the range 0 - 59"));
        }

        if (this.second != null && (0 > this.second || this.second > 59)) {
            throw new AppException(new ErrorResponse("The value of the second must be in the range 0 - 59"));
        }

        if (this.keepRecordBackup == null) {
            throw new AppException(new ErrorResponse("The value of the keepRecordBackup must be not null"));
        }

        if (EnvConfig.BK_MIN_KEEP_RECORD > this.keepRecordBackup || this.keepRecordBackup > EnvConfig.BK_MAX_KEEP_RECORD) {
            throw new AppException(new ErrorResponse("The value of the keepRecordBackup must be in the range %s - %s",
                    EnvConfig.BK_MIN_KEEP_RECORD, EnvConfig.BK_MAX_KEEP_RECORD));
        }

        String intervalType = CommonUtils.calculateIntervalType(this.hour, this.minute, this.second);

        if (this.interval == null) {
            throw new AppException(new ErrorResponse("The value of the interval must be not null"));
        }

        switch (intervalType) {
            case "DAY" -> {
                if (1 > this.interval || this.interval > 10) {
                    throw new AppException(new ErrorResponse("The value of the interval must be in the range 1 - 10"));
                }
            }
            case "HOUR" -> {
                if (1 > this.interval || this.interval > 23) {
                    throw new AppException(new ErrorResponse("The value of the interval must be in the range 1 - 23"));
                }
            }
            case "MINUTE" -> {
                if (1 > this.interval || this.interval > 59) {
                    throw new AppException(new ErrorResponse("The value of the interval must be in the range 1 - 59"));
                }
            }
        }

        this.setIntervalType(intervalType);
    }

}

package com.ctel.dbaas.dto.backup;

import lombok.Data;

@Data
public class BackupScheduleRes {

    private String id;

    private String instanceId;

    private String instanceName;

    private Integer hour;

    private Integer minute;

    private Integer second;

    private String timeZone;

    private Integer intervalNum;

    private String intervalType;

    private String lastBackupTime;

    private String nextBackupTime;

    private Integer keepRecordBackup;

    private String createdAt;

    private String updatedAt;

}

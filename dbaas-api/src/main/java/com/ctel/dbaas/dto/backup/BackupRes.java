package com.ctel.dbaas.dto.backup;

import lombok.Data;

@Data
public class BackupRes {

    private String id;

    private String instanceId = "";

    private String instanceName = "";

    private String backupName;

    private String datastoreName;

    private String datastoreCode;

    private String datastoreVersion;

    private Long size = 0L;

    private String backupStrategyType;

    private String status;

    private String backupStartAt;

    private String backupEndAt;

    private String created;

}

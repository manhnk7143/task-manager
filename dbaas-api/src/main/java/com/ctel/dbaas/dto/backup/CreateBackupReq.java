package com.ctel.dbaas.dto.backup;

import lombok.Data;

@Data
public class CreateBackupReq {

    private String name;

    private String instanceId;

    private String backupStrategyType;

}

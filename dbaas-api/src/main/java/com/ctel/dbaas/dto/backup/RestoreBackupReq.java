package com.ctel.dbaas.dto.backup;

import lombok.Data;

@Data
public class RestoreBackupReq {

    private String backupId;

    private String instanceId;

}

package com.ctel.dbaas.dto.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TriggerBackupRequest {

    private String scheduleBackupId;

    private String timeExec;

}
